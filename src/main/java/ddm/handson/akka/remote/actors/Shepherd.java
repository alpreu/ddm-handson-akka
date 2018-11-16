package ddm.handson.akka.remote.actors;

import akka.actor.*;
import ddm.handson.akka.remote.messages.ShutdownMessage;

import java.io.Serializable;
import java.util.HashSet;

public class Shepherd extends AbstractLoggingActor {

    public static class SubscriptionMessage implements Serializable {
        public final int numberOfWorkers;

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

    private void handle(SubscriptionMessage message) {
        final ActorRef slave = sender();
        if (!slaves.add(slave))
            return;

        slave.tell(new Slave.AcknowledgementMessage(), self());
        context().watch(slave);
        Address remoteAddress = sender().path().address();
        master.tell(new Master.RemoteSystemMessage(remoteAddress, message.numberOfWorkers), self());
    }


}
