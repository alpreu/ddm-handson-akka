package ddm.handson.akka;
import org.apache.commons.cli.*;

public class App {
    private static final String SYSTEM_NAME = "handson";
    private static final String MASTER_HOST = "127.0.0.1";
    private static final String SLAVE_HOST = "127.0.0.1";
    private static final int MASTER_PORT = 7877;
    private static final int SLAVE_PORT = 7879;


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

            if (nodeType.equals(Master.MASTER_ROLE)) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue("workers"));
                int numberOfSlaves = Integer.parseInt(line.getOptionValue("slaves"));
                String inputFilename = line.getOptionValue("input");

                Master.start(SYSTEM_NAME,numberOfWorkers, MASTER_HOST, MASTER_PORT);
            } else if (nodeType.equals(Slave.SLAVE_ROLE)) {
                int numberOfWorkers = Integer.parseInt(line.getOptionValue("workers"));
                String hostAddress = line.getOptionValue("host");

                Slave.start(SYSTEM_NAME, numberOfWorkers, SLAVE_HOST, SLAVE_PORT, MASTER_HOST, MASTER_PORT);
            } else {
                System.err.println("First argument must specify type: '" + Master.MASTER_ROLE + "' or '" + Slave.SLAVE_ROLE + "'");
            }
        } catch (Exception e) {
            System.err.println("Parsing arguments failed. Error: " + e.getMessage());
        }
    }
}
