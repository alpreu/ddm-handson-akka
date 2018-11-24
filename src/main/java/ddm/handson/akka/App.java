package ddm.handson.akka;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ddm.handson.akka.remote.actors.Worker;
import org.apache.commons.cli.*;
import akka.actor.ActorSystem;

import java.util.concurrent.TimeoutException;

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
        Option hostOpt = Option.builder()
                .longOpt("host")
                .hasArg()
                .valueSeparator(' ')
                .build();
        Option portOpt = Option.builder()
                .longOpt("port")
                .hasArg()
                .valueSeparator(' ')
                .optionalArg(true)
                .build();
        Option masterPortOpt = Option.builder()
                .longOpt("master-port")
                .hasArg()
                .valueSeparator(' ')
                .optionalArg(true)
                .build();

        options.addOption(workers);
        options.addOption(slaves);
        options.addOption(input);
        options.addOption(hostOpt);
        options.addOption(portOpt);
        options.addOption(masterPortOpt);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            String nodeType = line.getArgList().get(0);

            int port = -1;
            int masterPort = -1;

            if (line.hasOption("port"))
                port = Integer.parseInt(line.getOptionValue("port"));

            if (line.hasOption("master-port"))
                masterPort = Integer.parseInt(line.getOptionValue("master-port"));

            if (nodeType.equals("master")) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue("workers"));
                int numberOfSlaves = Integer.parseInt(line.getOptionValue("slaves"));
                String inputFilename = line.getOptionValue("input");
                String host = line.hasOption("host") ? line.getOptionValue("host") : "";
                runAsMaster(host, port, numberOfWorkers, numberOfSlaves, inputFilename);
            } else if (nodeType.equals("slave")) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue("workers"));
                String hostAddress = line.getOptionValue("host");
                runAsSlave(hostAddress, port, masterPort, numberOfWorkers);
            } else {
                System.err.println("First argument must specify type: 'master' or 'slave'");
            }
        } catch (Exception e) {
            System.err.println("Parsing arguments failed. Error: " + e.getMessage());
        }

        System.out.println("Exiting program");
    }



    private static void runAsMaster(String host, int port, int numberOfWorkers, int numberOfSlaves, String inputFile)
    {
        // Master erstellen

        MasterActorSystem master = new MasterActorSystem(host, port, numberOfWorkers, numberOfSlaves, inputFile);
        try {
            master.awaitTermination();
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runAsSlave(String masterHost, int port, int masterPort, int numberOfWorkers)
    {
        // Master erstellen

        SlaveActorSystem slaveActorSystem = new SlaveActorSystem(port, masterHost, masterPort, numberOfWorkers);

        try {
            slaveActorSystem.awaitTermination();
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
