package ddm.handson.akka;
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
        system.shutdown();
    }
}
