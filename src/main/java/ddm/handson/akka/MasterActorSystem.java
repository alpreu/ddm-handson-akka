package ddm.handson.akka;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import ddm.handson.akka.actors.ClusterListener;
import ddm.handson.akka.actors.Master;
import ddm.handson.akka.actors.Worker;
import ddm.handson.akka.actors.SystemRegisterer;
import ddm.handson.akka.util.ProblemEntry;

import java.io.IOException;
import java.util.List;

public class MasterActorSystem extends HandsonSystem {
    public static final String MASTER_ROLE = "master";

    public static void start(String systemName, int numberOfWorkers, String host, int port, int numberOfSlaves, String inputFilename) {
        final Config config = createConfig(systemName,MASTER_ROLE, host, port, host, port);
        final ActorSystem system = createSystem(systemName, config);

        Cluster.get(system).registerOnMemberUp(() -> { //only waits for the specified systems to become available, does not wait for the actors of each system...
            System.out.println("MasterActorSystem going up because system is ready");


            List<ProblemEntry> problemEntries = null;
            try {
                problemEntries = ProblemEntry.parseFile(inputFilename);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }


            system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);
            system.actorOf(Master.props(numberOfSlaves + 1, problemEntries), Master.DEFAULT_NAME);
            system.actorOf(SystemRegisterer.props(numberOfWorkers));

            for (int i = 0; i < numberOfWorkers; i++) {
                system.actorOf(Worker.props(), Worker.DEFAULT_NAME + "_master_" + i);
            }
        });
    }
}
