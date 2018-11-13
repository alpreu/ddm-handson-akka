package ddm.handson.akka.remote.actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class Slave extends AbstractLoggingActor {


    @Override
    public AbstractActor.Receive createReceive() {
        return null;
    }

    public static Props props()
    {
        return Props.create(Slave.class);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }
}
