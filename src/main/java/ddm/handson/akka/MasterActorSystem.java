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
    public static final int DEFAULT_PORT = 7877;
    public static final String DEFAULT_ROLE = "master";


    private final ActorSystem system;
    private final ActorRef master;

    // Ein master besteht aus 1 x Listener, 1 x Shepherd, 1 x Reaper, n x Workers

    public MasterActorSystem(String host, int port, int numberOfWorkers, int numberOfSlaves, String problemFile) {
        System.out.println("Creating " + DEFAULT_NAME);

        if (port <= 0)
            port = DEFAULT_PORT;

        if (host == "")
            host = Utils.getLocalHost();

        final Config config = Utils.createRemoteAkkaConfig(host, port);
        system = ActorSystem.create(DEFAULT_NAME, config);

        // lade problem file
        List<ProblemEntry> problemEntries = null;
        try {
            problemEntries = ProblemEntry.parseFile(problemFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);
        master = system.actorOf(Master.props(numberOfWorkers, numberOfSlaves, problemEntries), Master.DEFAULT_NAME);
        system.actorOf(Shepherd.props(master), Shepherd.DEFAULT_NAME);

        master.tell(new Master.Solve(), ActorRef.noSender());
    }

    public void awaitTermination() throws TimeoutException, InterruptedException {
        Await.ready(system.whenTerminated(), Duration.Inf());
        System.out.println(DEFAULT_NAME + " terminated.");
    }
}
