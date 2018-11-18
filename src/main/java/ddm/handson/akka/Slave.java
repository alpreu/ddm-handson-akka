package ddm.handson.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import ddm.handson.akka.actors.Worker;

public class Slave extends HandsonSystem {
    public static final String SLAVE_ROLE = "slave";

    public static void start(String systemName, int workers, String host, int port, String masterHost, int masterPort) {
        final Config config = createConfig(systemName, SLAVE_ROLE, host, port, masterHost, masterPort);
        final ActorSystem system = createSystem(systemName, config);

        Cluster.get(system).registerOnMemberUp(() -> {
            System.out.println("Slave going up because system is ready");
            for (int i = 0; i < workers; i++) {
                system.actorOf(Worker.props(), Worker.DEFAULT_NAME + "_slave_" + i);
            }
        });
    }

}
