package ddm.handson.akka.remote.actors;

import akka.actor.*;

import java.io.Serializable;
import java.util.HashSet;

public class Reaper extends AbstractLoggingActor {

    public static final String DEFAULT_NAME = "reaper";

    private HashSet<ActorRef> watchees;

    public Reaper()
    {
        watchees = new HashSet<>();
    }

    public static class WatchMeMessage implements Serializable { }

    public static void watchWithDefaultReaper(AbstractActor actor) {
        ActorSelection defaultReaper = actor.getContext().getSystem().actorSelection("/user/" + DEFAULT_NAME);
        defaultReaper.tell(new WatchMeMessage(), actor.getSelf());
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(WatchMeMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .build();
    }

    private void handle(WatchMeMessage message) {
        final ActorRef sender = this.getSender();
        if (watchees.add(sender)) {
            this.getContext().watch(sender);
            this.log().info("Started watching {}", sender);
        }
    }

    private void handle (Terminated message) {
        final ActorRef sender = this.getSender();

        if (watchees.remove(sender)) {
            this.log().info("{} has terminated", sender);

            if (watchees.isEmpty()) {
                this.log().info("All actors have terminated. Terminating the ActorSystem: {}",
                        this.getContext().getSystem().name());
                this.getContext().getSystem().terminate();
            }
        }
    }


    @Override
    public void preStart() throws Exception {
        super.preStart();
        this.log().info("Starting " + DEFAULT_NAME);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        this.log().info("Stopping " + DEFAULT_NAME);
    }

    public static Props props()
    {
        return Props.create(Reaper.class);
    }
}
