package ddm.handson.akka.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static String hash(int number) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(String.valueOf(number).getBytes("UTF-8"));

            StringBuffer stringBuffer = new StringBuffer(64);
            for (int i = 0; i < hashedBytes.length; i++) {
                stringBuffer.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuffer.toString();
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
