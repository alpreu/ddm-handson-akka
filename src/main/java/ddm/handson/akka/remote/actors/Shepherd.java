package ddm.handson.akka.remote.actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class Shepherd extends AbstractLoggingActor {

    public static final String DEFAULT_NAME = "shepherd";

    @Override
    public AbstractActor.Receive createReceive() {
        return null;
    }

    public static Props props()
    {
        return Props.create(Shepherd.class);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }
}
