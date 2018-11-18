package ddm.handson.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import com.typesafe.config.Config;
import ddm.handson.akka.remote.actors.Reaper;
import ddm.handson.akka.remote.actors.Slave;
import ddm.handson.akka.util.Utils;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeoutException;

public class SlaveActorSystem {

    public static final String DEFAULT_NAME = "SlaveActorSystem";
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 33334;

    private final ActorSystem system;

    public SlaveActorSystem(String masterHost, int masterPort, int numberOfWorkers) {

        final Config config = Utils.createConfiguration(DEFAULT_HOST, DEFAULT_PORT);
        system = ActorSystem.create(DEFAULT_NAME, config);

        system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);

        final ActorRef slave = system.actorOf(Slave.props(numberOfWorkers), Slave.DEFAULT_NAME);

        slave.tell(new Slave.AddressMessage(new Address("akka.tcp", MasterActorSystem.DEFAULT_NAME,
                masterHost, masterPort)), ActorRef.noSender());
    }


    public void awaitTermination() throws TimeoutException, InterruptedException {
        Await.ready(system.whenTerminated(), Duration.Inf());
        System.out.println(DEFAULT_NAME + " terminated.");
    }
}
