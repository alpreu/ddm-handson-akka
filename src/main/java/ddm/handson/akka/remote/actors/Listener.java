package ddm.handson.akka.remote.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class Listener extends AbstractLoggingActor {

    public static final String DEFAULT_NAME = "listener";

    @Override
    public Receive createReceive() {
        return null;
    }

    public static Props props()
    {
        return Props.create(Listener.class);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }
}
