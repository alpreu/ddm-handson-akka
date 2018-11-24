package ddm.handson.akka.remote.actors;

import akka.actor.*;
import ddm.handson.akka.remote.messages.RemoteSystemMessage;
import ddm.handson.akka.remote.messages.ShutdownMessage;

import java.io.Serializable;
import java.util.HashSet;

public class Shepherd extends AbstractLoggingActor {

    public static class SubscriptionMessage implements Serializable {
        public final int numberOfWorkers;

        @SuppressWarnings("unused")
        public SubscriptionMessage() {
            this(0);
        }
        public SubscriptionMessage(int numberOfWorkers) {
            this.numberOfWorkers = numberOfWorkers;
        }
    }

    public static final String DEFAULT_NAME = "shepherd";

    private final HashSet<ActorRef> slaves;

    private final ActorRef master;

    public Shepherd(ActorRef master)
    {
        this.master = master;
        slaves = new HashSet<>(42);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SubscriptionMessage.class, this::handle)
                .match(ShutdownMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .build();
    }

    private void handle(Terminated terminated) {
        final ActorRef sender = this.getSender();
        this.slaves.remove(sender);
    }

    private void handle(ShutdownMessage shutdownMessage) {
        log().warning("{} received shutdown message.", DEFAULT_NAME);
        for (ActorRef a : slaves) {
            a.tell(new ShutdownMessage(), self());
        }
        self().tell(PoisonPill.getInstance(), self());
    }

    public static Props props(ActorRef master)
    {
        return Props.create(Shepherd.class, master);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        // Stop all slaves that connected to this Shepherd
        for (ActorRef slave : this.slaves)
            slave.tell(PoisonPill.getInstance(), this.getSelf());

        // Log the stop event
        this.log().debug("Stopped {}.", this.getSelf());
    }

    private void handle(SubscriptionMessage message) {
        final ActorRef slave = sender();
        if (!slaves.add(slave))
            return;

        slave.tell(new Slave.AcknowledgementMessage(), self());
        context().watch(slave);
        Address remoteAddress = sender().path().address();
        master.tell(new RemoteSystemMessage(remoteAddress, message.numberOfWorkers), self());
    }


}
