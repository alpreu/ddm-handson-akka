package ddm.handson.akka.remote.actors;

import akka.actor.*;
import akka.remote.DisassociatedEvent;
import ddm.handson.akka.remote.messages.ShutdownMessage;
import scala.concurrent.ExecutionContextExecutor;

import java.io.Serializable;
import java.time.Duration;

public class Slave extends AbstractLoggingActor {

    public static class AddressMessage implements Serializable {
        public final Address address;

        public AddressMessage(Address address) {
            this.address = address;
        }
    }

    public static class AcknowledgementMessage implements Serializable {
    }

    public static final String DEFAULT_NAME = "slave";

    private Cancellable connectSchedule;

    public final int numberOfWorkers;


    public Slave(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
        connectSchedule = null;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(AddressMessage.class, this::handle)
                .match(AcknowledgementMessage.class, this::handle)
                .match(DisassociatedEvent.class, this::handle)
                .match(ShutdownMessage.class, this::handle)
                .build();
    }

    private void handle(ShutdownMessage shutdownMessage) {
        // Log remote shutdown message
        this.log().info("Was asked to stop.");

        // Stop self by sending a poison pill
        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());
    }

    private void handle(DisassociatedEvent disassociatedEvent) {
        // Disassociations are a problem only once we have a running connection, i.e., no connection schedule is active; they do not concern this actor otherwise.
        if (this.connectSchedule == null) {
            this.log().error("Disassociated from master. Stopping...");
            this.getContext().stop(this.getSelf());
        }
    }

    private void handle(AcknowledgementMessage acknowledgementMessage) {
        CancelConnectSchedule();
        this.log().info("Subscription successfully acknowledged by {}.", this.getSender());
    }

    private void handle(AddressMessage message) {
        CancelConnectSchedule();

        final ActorSelection shepherd = getContext().getSystem()
                .actorSelection(String.format("%s/user/%s", message.address, Shepherd.DEFAULT_NAME));

        final Scheduler scheduler = getContext().getSystem().scheduler();
        final ExecutionContextExecutor dispatcher = getContext().getSystem().dispatcher();
        connectSchedule = scheduler.schedule(
                Duration.ZERO,
                Duration.ofSeconds(5),
                () -> shepherd.tell(new Shepherd.SubscriptionMessage(numberOfWorkers), self()),
                dispatcher
        );

    }

    public static Props props(int numberOfWorkers)
    {
        return Props.create(Slave.class, numberOfWorkers);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }

    private void CancelConnectSchedule()
    {
        if (this.connectSchedule != null) {
            this.connectSchedule.cancel();
            this.connectSchedule = null;
        }
    }
}
