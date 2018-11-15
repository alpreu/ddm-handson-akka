package ddm.handson.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import ddm.handson.akka.actors.ClusterListener;
import ddm.handson.akka.actors.Worker;

public class Master extends HandsonSystem {
    public static final String MASTER_ROLE = "master";

    public static void start(String systemName, int workers, String host, int port) {
        final Config config = createConfig(systemName,MASTER_ROLE, host, port, host, port);
        final ActorSystem system = createSystem(systemName, config);

        Cluster.get(system).registerOnMemberUp(() -> {
            System.out.println("Master: onMemberUp Callback ran.");
            system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);

            for (int i = 0; i < workers; i++) {
                system.actorOf(Worker.props(), Worker.DEFAULT_NAME + "_master_" + i);
            }
        });

        system.actorSelection("/user/*").tell(new Worker.WorkMessage("Workmessage from the master."), ActorRef.noSender());
    }
}
