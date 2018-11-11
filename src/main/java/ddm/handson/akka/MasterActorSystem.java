package ddm.handson.akka;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import ddm.handson.akka.remote.actors.*;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeoutException;

public class MasterActorSystem {

    private static final String DEFAULT_NAME = "MasterActorSystem";
    private static final int DEFAULT_PORT = 64321;
    private static final String DEFAULT_ROLE = "master";
    private static final String DEFAULT_HOST = "127.0.0.1";

    private final int numberOfWorkers;
    private final int numberOfSlaves;
    private final String problemFile;

    private final ActorSystem system;
    private final Listener listener;
    private final Shepherd shepherd;
    private final Reaper reaper;
    private final Worker[] workers;
    private final Slave[] slaves;



    // Ein master besteht aus 1 x Listener, 1 x Shepherd, 1 x Reaper, n x Workers

    public MasterActorSystem(int numberOfWorkers, int numberOfSlaves, String problemFile) {
        System.out.println("Creating " + DEFAULT_NAME);
        this.numberOfWorkers = numberOfWorkers;
        this.numberOfSlaves = numberOfSlaves;
        this.problemFile = problemFile;

        final Config config = Utils.createConfiguration(DEFAULT_HOST, DEFAULT_PORT);
        system = ActorSystem.create(DEFAULT_NAME, config);

        listener = new Listener();
        shepherd = new Shepherd();
        reaper = new Reaper();
        workers = new Worker[this.numberOfWorkers];

        for (int i = 0; i < this.numberOfWorkers; ++i)
        {
            workers[i] = new Worker();
        }

        // Wait for slaves to connect;
        slaves = null;
    }

    public void awaitTermination() throws TimeoutException, InterruptedException {
        Await.ready(system.whenTerminated(), Duration.Inf());
        System.out.println(DEFAULT_NAME + " terminated.");
    }
}
