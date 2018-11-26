package ddm.handson.akka;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


public class HandsonSystem {

    protected static Config createConfig(String systemName, String systemRole, String host,
                                         int port, String masterHost, int masterPort) {
        return ConfigFactory.parseString(
                "akka.remote.netty.tcp.hostname = \"" + host + "\"\n" +
                        "akka.remote.netty.tcp.port = " + port + "\n" +
                        "akka.remote.artery.canonical.hostname = \"" + host + "\"\n" +
                        "akka.remote.artery.canonical.port = " + port + "\n" +
                        "akka.cluster.roles = [" + systemRole + "]\n" +
                        "akka.cluster.seed-nodes = [\"akka://" + systemName + "@" + masterHost + ":" + masterPort + "\"]")
                .withFallback(ConfigFactory.load("default"));

    }

    protected static Config createConfig(String systemName, String systemRole, String host,
                                         int port, String masterHost, int masterPort, int numberOfSlaves) {
        return ConfigFactory.parseString(
                "akka.remote.netty.tcp.hostname = \"" + host + "\"\n" +
                        "akka.remote.netty.tcp.port = " + port + "\n" +
                        "akka.remote.artery.canonical.hostname = \"" + host + "\"\n" +
                        "akka.remote.artery.canonical.port = " + port + "\n" +
                        "akka.cluster.roles = [" + systemRole + "]\n" +
                        "akka.cluster.role.slave.min-nr-of-members = " + numberOfSlaves + "\n" +
                        "akka.cluster.seed-nodes = [\"akka://" + systemName + "@" + masterHost + ":" + masterPort + "\"]")
                .withFallback(ConfigFactory.load("default"));

    }

    protected static ActorSystem createSystem(String systemName, Config config) {
        final ActorSystem system = ActorSystem.create(systemName, config);

        // end program when ActorSystem terminates
        system.registerOnTermination(() -> System.exit(0));

        // terminate ActorSystem in case it gets detached from cluster
        Cluster.get(system).registerOnMemberRemoved(() -> {
                    system.terminate();

                    new Thread(() -> {
                        try {
                            Thread.sleep(10000);
                        } catch (Exception e) {
                            System.exit(-1);
                        }
                    }).start();
                }
        );

        return system;
    }
}
