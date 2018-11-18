package ddm.handson.akka.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import ddm.handson.akka.messages.CompletionMessage;
import ddm.handson.akka.messages.RegistrationMessage;
import ddm.handson.akka.messages.TaskMessage;
import ddm.handson.akka.messages.WorkMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Profiler extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "profiler";

    public static Props props() {
        return Props.create(Profiler.class);
    }

    private final Queue<WorkMessage> unassignedWork = new LinkedList<>();
    private final Queue<ActorRef> idleWorkers =  new LinkedList<>();
    private final Map<ActorRef, WorkMessage> busyWorkers = new HashMap<>();

    private TaskMessage task;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegistrationMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .match(TaskMessage.class, this::handle)
                .match(CompletionMessage.class, this::handle)
                .matchAny(object -> this.log().info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(RegistrationMessage message) {
        this.context().watch(this.sender());

        this.assign(this.sender());
        this.log().info("Registered {}", this.sender());
    }

    private void handle(Terminated message) {
        this.context().unwatch(message.getActor());

        if (!this.idleWorkers.remove(message.getActor())) {
            WorkMessage work = this.busyWorkers.remove(message.getActor());
            if (work != null) {
                this.assign(work);
            }
        }
        this.log().info("Unregistered {}", message.getActor());
    }

    private void handle(TaskMessage message) {
        if (this.task != null)
            this.log().error("The profiler actor can process only one task in its current implementation.");
        this.task = message;
        this.assign(new WorkMessage(message.content));
    }

    private void handle(CompletionMessage message) {
        ActorRef worker = this.sender();
        WorkMessage work = this.busyWorkers.remove(worker);

        this.log().info("Completed work: {}", work.toString());

        switch (message.getResult()) {
            case OK:
                //do nothing
                break;
            case FAILED:
                this.assign(work);
                break;
        }
        this.assign(worker);
    }

    private void assign(WorkMessage work) {
        ActorRef worker = this.idleWorkers.poll();
        if (worker == null) {
            this.unassignedWork.add(work);
            return;
        }

        this.busyWorkers.put(worker, work);
        worker.tell(work, this.self());
    }

    private void assign(ActorRef worker) {
        WorkMessage work = this.unassignedWork.poll();
        if (work == null) {
            this.idleWorkers.add(worker);
            return;
        }

        this.busyWorkers.put(worker, work);
        worker.tell(work, this.self());
    }





}
