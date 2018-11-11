package ddm.handson.akka;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Utils {
    // TODO: Extend to what we really need
    public static Config createConfiguration(String host, int port) {

        // Create the Config with fallback to the application config
        return ConfigFactory.parseString(
                "akka.remote.netty.tcp.hostname = \"" + host + "\"\n" +
                        "akka.remote.netty.tcp.port = " + port + "\n");
    }
}
