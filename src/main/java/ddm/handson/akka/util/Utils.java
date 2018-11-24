package ddm.handson.akka.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    /**
     * Binding to replace variables in our pimped {@code .conf} files.
     */
    private static class VariableBinding {

        private final String pattern, value;

        private VariableBinding(String variableName, Object value) {
            this.pattern = Pattern.quote("$" + variableName);
            this.value = Objects.toString(value);
        }

        private String apply(String str) {
            return str.replaceAll(this.pattern, this.value);
        }
    }

    /**
     * Load a {@link Config}.
     *
     * @param resource the path of the config resource
     * @return the {@link Config}
     */
    private static Config loadConfig(String resource, VariableBinding... bindings) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new FileNotFoundException("Could not get the resource " + resource);
            }
            Stream<String> content = new BufferedReader(new InputStreamReader(in)).lines();
            for (VariableBinding binding : bindings) {
                content = content.map(binding::apply);
            }
            String result = content.collect(Collectors.joining("\n"));
            return ConfigFactory.parseString(result);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load resource " + resource);
        }
    }


    public static Config createRemoteAkkaConfig(String host, int port) {
        Config config = loadConfig(
                "base.conf",
                new VariableBinding("host", host),
                new VariableBinding("port", port)
        );
        return config;
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

    public static String getLocalHost()
    {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
