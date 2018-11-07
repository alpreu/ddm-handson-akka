package ddm.handson.akka;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.cli.*;
import akka.actor.ActorSystem;

public class App {
    public static void main(String[] args) {
        Options options = new Options();
        Option workers = Option.builder()
                .longOpt("workers")
                .hasArg()
                .valueSeparator(' ')
                .build();
        Option slaves = Option.builder()
                .longOpt("slaves")
                .hasArg()
                .valueSeparator(' ')
                .build();
        Option input = Option.builder()
                .longOpt("input")
                .hasArg()
                .valueSeparator(' ')
                .build();
        Option host = Option.builder()
                .longOpt("host")
                .hasArg()
                .valueSeparator(' ')
                .build();

        options.addOption(workers);
        options.addOption(slaves);
        options.addOption(input);
        options.addOption(host);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            String nodeType = line.getArgList().get(0);

            if (nodeType.equals("master")) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue("workers"));
                int numberOfSlaves = Integer.parseInt(line.getOptionValue("slaves"));
                String inputFilename = line.getOptionValue("input");
                startMaster(numberOfWorkers, numberOfSlaves, inputFilename);
            } else if (nodeType.equals("slave")) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue("workers"));
                String hostAddress = line.getOptionValue("host");
            } else {
                System.err.println("First argument must specify type: 'master' or 'slave'");
            }
        } catch (Exception e) {
            System.err.println("Parsing arguments failed. Error: " + e.getMessage());
        }

        ActorSystem system = ActorSystem.create("ExampleActorSystem");
        System.out.println("Successfully created actor system: " + system.name() + ".");
        system.terminate();
    }

    protected static Config createConfiguration(String actorSystemName, String actorSystemRole, String host, int port, String masterhost, int masterport) {

        // Create the Config with fallback to the application config
        return ConfigFactory.parseString(
                "akka.remote.netty.tcp.hostname = \"" + host + "\"\n" +
                        "akka.remote.netty.tcp.port = " + port + "\n" +
                        "akka.remote.artery.canonical.hostname = \"" + host + "\"\n" +
                        "akka.remote.artery.canonical.port = " + port + "\n" +
                        "akka.cluster.roles = [" + actorSystemRole + "]\n" +
                        "akka.cluster.seed-nodes = [\"akka://" + actorSystemName + "@" + masterhost + ":" + masterport + "\"]")
                .withFallback(ConfigFactory.load("octopus"));
    }

    private static void startMaster(int numberOfWorkers, int numberOfSlaves, String inputFile)
    {
        System.out.println("Starting master");
        final Config config = createConfiguration("master", "master", "127.0.0.1",
                64231, "127.0.0.1", 64231);
        final ActorSystem actorSystem = ActorSystem.create("master", config);

        ActorRef worker = actorSystem.actorOf(Props.create(Worker.class), "worker");
        worker.tell(new TextMessage("Hi from master!"), actorSystem.);



        actorSystem.terminate();
    }
}
