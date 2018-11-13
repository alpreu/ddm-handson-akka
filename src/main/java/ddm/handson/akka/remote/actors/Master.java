package ddm.handson.akka.remote.actors;

import akka.actor.*;
import ddm.handson.akka.remote.messages.ShutdownMessage;

public class Master extends AbstractLoggingActor {

    public static final String DEFAULT_NAME = "master";

    private final ActorRef listener;

    public Master(final ActorRef listener, final int numberOfWorkers)
    {
        this.listener = listener;
        for (int i = 0; i < numberOfWorkers; ++i)
        {
            final ActorRef worker = getContext().actorOf(Worker.props());
            this.getContext().watch(worker);
        }

    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Terminated.class, this::handle)
                .build();

    }

    private void handle(Terminated message) {
        final ActorRef sender = this.getSender();
        this.log().warning("Worker {} terminated.", sender);

        if (hasFinished()) {
            stopSelfAndListener();
        }
    }

    private boolean hasFinished() {
        return true;
    }

    private void stopSelfAndListener() {
        // Work is done. We don't need the listener anymore
        this.listener.tell(new ShutdownMessage(), this.getSelf());

        // Stop self and all child actors by sending a poison pill.
        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());
    }

    public static Props props()
    {
        return Props.create(Master.class);
    }
}
