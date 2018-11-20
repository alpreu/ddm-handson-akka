package ddm.handson.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import ddm.handson.akka.actors.ClusterListener;
import ddm.handson.akka.actors.Profiler;
import ddm.handson.akka.actors.Worker;
import ddm.handson.akka.actors.WorkerRegisterer;
import ddm.handson.akka.messages.TaskMessage;

public class Master extends HandsonSystem {
    public static final String MASTER_ROLE = "master";

    public static void start(String systemName, int workers, String host, int port, int numberOfSlaves) {
        final Config config = createConfig(systemName,MASTER_ROLE, host, port, host, port);
        final ActorSystem system = createSystem(systemName, config);

        Cluster.get(system).registerOnMemberUp(() -> { //only waits for the specified systems to become available, does not wait for the actors of each system...
            System.out.println("Master going up because system is ready");

            system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);
            system.actorOf(Profiler.props(numberOfSlaves + 1), Profiler.DEFAULT_NAME);
            system.actorOf(WorkerRegisterer.props(workers));

            for (int i = 0; i < workers; i++) {
                system.actorOf(Worker.props(), Worker.DEFAULT_NAME + "_master_" + i);
            }

            for (int i = 0; i < 100000; i++) {
                String messageContent = "workPacket #" + i;
                system.actorSelection("/user/" + Profiler.DEFAULT_NAME).tell(new TaskMessage(messageContent), ActorRef.noSender());
            }


        });
    }
}
