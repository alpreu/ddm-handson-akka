package ddm.handson.akka;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;

public class Slave extends HandsonSystem {
    public static final String SLAVE_ROLE = "slave";

    public static void start(String systemName, int workers, String host, int port, String masterHost, int masterPort) {
        final Config config = createConfig(systemName, SLAVE_ROLE, host, port, masterHost, masterPort);
        final ActorSystem system = createSystem(systemName, config);

        Cluster.get(system).registerOnMemberUp(() -> {
            //TODO
            System.out.println("Slave: OnMemberUp Callback ran.");
        });
    }

}
