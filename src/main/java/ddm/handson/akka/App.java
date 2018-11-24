package ddm.handson.akka;
import ddm.handson.akka.util.Utils;
import org.apache.commons.cli.*;

public class App {
    private static final String SYSTEM_NAME = "handson";
    private static final String WORKERSOPT = "workers";
    private static final String SLAVESOPT = "slaves";
    private static final String INPUTOPT = "input";
    private static final String MASTERHOSTOPT = "masterHost";
    private static final String MASTERPORTOPT = "masterPort";
    private static final String SLAVEPORTOPT = "slavePort";


    public static void main(String[] args) {
        Options options = new Options();
        Option workersOpt = Option.builder()
                .longOpt(WORKERSOPT)
                .hasArg()
                .build();
        Option slavesOpt = Option.builder()
                .longOpt(SLAVESOPT)
                .hasArg()
                .build();
        Option inputOpt = Option.builder()
                .longOpt(INPUTOPT)
                .hasArg()
                .build();
        Option masterHostOpt = Option.builder()
                .longOpt(MASTERHOSTOPT)
                .hasArg()
                .build();
        Option masterPortOpt = Option.builder()
                .longOpt(MASTERPORTOPT)
                .hasArg()
                .build();
        Option slavePortOpt = Option.builder()
                .longOpt(SLAVEPORTOPT)
                .hasArg()
                .build();

        options.addOption(workersOpt);
        options.addOption(slavesOpt);
        options.addOption(inputOpt);
        options.addOption(masterHostOpt);
        options.addOption(masterPortOpt);
        options.addOption(slavePortOpt);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            String nodeType = line.getArgList().get(0);

            if (nodeType.equals(MasterActorSystem.MASTER_ROLE)) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue(WORKERSOPT, "0"));
                int numberOfSlaves = Integer.parseInt(line.getOptionValue(SLAVESOPT, "0"));
                String masterHost = Utils.getLocalHost();
                int masterPort = Integer.parseInt(line.getOptionValue(MASTERPORTOPT, "0"));
                String inputFilename = line.getOptionValue(INPUTOPT, "");


                System.out.println(numberOfWorkers);
                System.out.println(numberOfSlaves);
                System.out.println(masterHost);
                System.out.println(masterPort);
                System.out.println(inputFilename);


                if (numberOfWorkers == 0 ||
                numberOfSlaves == 0 ||
                masterPort == 0 ||
                inputFilename.equals("")) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("java -jar handson-akka-1.0-SNAPSHOT.jar", options);
                    System.out.println("Example Usage: java -jar handson-akka-1.0-SNAPSHOT.jar master " +
                            "--workers 4 --slaves 2  --masterPort 7877 --input students.csv");
                    return;
                }

                MasterActorSystem.start(SYSTEM_NAME, numberOfWorkers, masterHost, masterPort, numberOfSlaves, inputFilename);
            } else if (nodeType.equals(SlaveActorSystem.SLAVE_ROLE)) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue(WORKERSOPT, "0"));
                String masterHost = line.getOptionValue(MASTERHOSTOPT, Utils.getLocalHost());
                int masterPort = Integer.parseInt(line.getOptionValue(MASTERPORTOPT, "0"));
                String slaveHost = Utils.getLocalHost();
                int slavePort = Integer.parseInt(line.getOptionValue(SLAVEPORTOPT, "0"));

                System.out.println(numberOfWorkers);
                System.out.println(masterHost);
                System.out.println(masterPort);
                System.out.println(slaveHost);
                System.out.println(slavePort);

                if (numberOfWorkers == 0 ||
                masterPort == 0 ||
                slavePort == 0) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("java -jar handson-akka-1.0-SNAPSHOT.jar", options);
                    System.out.println("Example Usage: java -jar handson-akka-1.0-SNAPSHOT.jar slave " +
                            "--workers 4 --masterPort 7877 --slavePort 7878");
                    return;
                }

                SlaveActorSystem.start(SYSTEM_NAME, numberOfWorkers, slaveHost, slavePort, masterHost, masterPort);
            } else {
                System.err.println("First argument must specify type: '" + MasterActorSystem.MASTER_ROLE + "' or '" + SlaveActorSystem.SLAVE_ROLE + "'");
            }
        } catch (Exception e) {
            System.err.println("Parsing arguments failed. Error: " + e.getMessage());
        }
    }
}
