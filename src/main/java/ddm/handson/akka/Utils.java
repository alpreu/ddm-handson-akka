package ddm.handson.akka;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Utils {
    // TODO: Extend to what we really need
    public static Config createConfiguration(String host, int port) {

        // Create the Config with fallback to the application config
        return ConfigFactory.parseString(
                "akka.remote.netty.tcp.hostname = \"" + host + "\"\n"
                        + "akka.remote.netty.tcp.port = " + port + "\n"
                        + "akka.remote.netty.tcp.message-frame-size =  30000000b\n"
                        + "akka.remote.netty.tcp.send-buffer-size =  30000000b\n"
                        + "akka.remote.netty.tcp.receive-buffer-size =  30000000b\n"
                        + "akka.remote.netty.tcp.maximum-frame-size = 30000000b\n"
                        + "akka.remote.enabled-transports = [\"akka.remote.netty.tcp\"]\n"
                        + "akka.actor.provider = remote\n"
                        + "akka.remote.maximum-payload-bytes = 30000000 bytes");
    }
}
