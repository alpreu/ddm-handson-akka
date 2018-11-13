package ddm.handson.akka;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;

public class Master extends HandsonSystem {
    public static final String MASTER_ROLE = "master";

    public static void start(String systemName, int workers, String host, int port) {
        final Config config = createConfig(systemName,MASTER_ROLE, host, port, host, port);
        final ActorSystem system = createSystem(systemName, config);

        Cluster.get(system).registerOnMemberUp(() -> {
            //TODO
            System.out.println("Master: onMemberUp Callback ran.");
        });
    }
}
