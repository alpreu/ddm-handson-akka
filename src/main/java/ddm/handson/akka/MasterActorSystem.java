package ddm.handson.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import ddm.handson.akka.remote.actors.Master;
import ddm.handson.akka.remote.actors.Reaper;
import ddm.handson.akka.remote.actors.Shepherd;
import ddm.handson.akka.util.ProblemEntry;
import ddm.handson.akka.util.Utils;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MasterActorSystem {

    public static final String DEFAULT_NAME = "MasterActorSystem";
    public static final int DEFAULT_PORT = 33333;
    public static final String DEFAULT_ROLE = "master";
    public static final String DEFAULT_HOST = "127.0.0.1";

    private final int numberOfWorkers;
    private final int numberOfSlaves;
    private final String problemFile;

    private final ActorSystem system;
    private final ActorRef master;
    private final ActorRef shepherd;
    private final ActorRef reaper;

    // Ein master besteht aus 1 x Listener, 1 x Shepherd, 1 x Reaper, n x Workers

    public MasterActorSystem(int numberOfWorkers, int numberOfSlaves, String problemFile) {
        System.out.println("Creating " + DEFAULT_NAME);
        this.numberOfWorkers = numberOfWorkers;
        this.numberOfSlaves = numberOfSlaves;
        this.problemFile = problemFile;

        final Config config = Utils.createConfiguration(DEFAULT_HOST, DEFAULT_PORT);
        system = ActorSystem.create(DEFAULT_NAME, config);

        // lade problem file
        List<ProblemEntry> problemEntries = null;
        try {
            problemEntries = ProblemEntry.parseFile(problemFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        reaper = system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);
        master = system.actorOf(Master.props(numberOfWorkers, numberOfSlaves, problemEntries), Master.DEFAULT_NAME);
        shepherd = system.actorOf(Shepherd.props(master), Shepherd.DEFAULT_NAME);

        master.tell(new Master.Solve(), ActorRef.noSender());
    }

    public void awaitTermination() throws TimeoutException, InterruptedException {
        Await.ready(system.whenTerminated(), Duration.Inf());
        System.out.println(DEFAULT_NAME + " terminated.");
    }
}
