package ddm.handson.akka;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import ddm.handson.akka.actors.ClusterListener;
import ddm.handson.akka.actors.Worker;
import ddm.handson.akka.actors.SystemRegisterer;

public class SlaveActorSystem extends HandsonSystem {
    public static final String SLAVE_ROLE = "slave";

    public static void start(String systemName, int workers, String host, int port, String masterHost, int masterPort) {
        final Config config = createConfig(systemName, SLAVE_ROLE, host, port, masterHost, masterPort);
        final ActorSystem system = createSystem(systemName, config);

        Cluster.get(system).registerOnMemberUp(() -> {
            System.out.println("SlaveActorSystem going up because system is ready");

            system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);
            system.actorOf(SystemRegisterer.props(workers));

            for (int i = 0; i < workers; i++) {
                system.actorOf(Worker.props(), Worker.DEFAULT_NAME + "_slave_" + i);
            }


        });
    }

}
