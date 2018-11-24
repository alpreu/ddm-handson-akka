package ddm.handson.akka.remote.actors;

import akka.actor.*;
import akka.remote.RemoteScope;
import akka.routing.*;
import ddm.handson.akka.remote.divider.*;
import ddm.handson.akka.remote.messages.*;
import ddm.handson.akka.util.ProblemEntry;

import java.io.Serializable;
import java.util.*;

public class Master extends AbstractLoggingActor {

    public static final String DEFAULT_NAME = "master";

    private final int expectedSlaves;
    private int connectedSlaves;
    private final HashSet<ActorRef> workers;

    private final List<ProblemEntry> problemEntries;
    private long startTime;
    private long endTime;
    private Router router;
    private PasswordCracker passwordCracker;
    private LCSCalculator lcsCalculator;
    private Hasher hasher;
    private LinearCombinationFinder lcFinder;
    private int lcMessagesInProgress;
    private int maxLCMessagesInProgress;
    private boolean printResultsAndShutdown;

    public static Props props(int numberOfWorkers, int numberOfSlaves, final List<ProblemEntry> problemEntries) {
        return Props.create(Master.class, numberOfWorkers, numberOfSlaves, problemEntries);
    }

    public Master(int numberOfWorkers, int expectedSlaves, final List<ProblemEntry> problemEntries) {
        this.expectedSlaves = expectedSlaves;
        connectedSlaves = 0;

        workers = new HashSet<>(512);

        for (int i = 0; i < numberOfWorkers; ++i) {
            final ActorRef worker = getContext().actorOf(Worker.props());
            addWorker(worker);
        }

        this.problemEntries = problemEntries;
        router = null;
        lcFinder = null;
        lcMessagesInProgress = 0;
        maxLCMessagesInProgress = 0;
        printResultsAndShutdown = false;
    }

    private void addWorker(final ActorRef worker)
    {
        context().watch(worker);
        workers.add(worker);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log().info("Master started");
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log().warning("Master stopped.");
        ActorSelection defaultShepherd = getContext().getSystem().actorSelection("/user/" + Shepherd.DEFAULT_NAME);
        defaultShepherd.tell(new ShutdownMessage(), self());
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(RemoteSystemMessage.class, this::handle)
                .match(Solve.class, this::handle)
                .match(SolveNextSubproblemMessage.class, this::handle)
                .match(FoundDecryptedPasswordsMessage.class, this::handle)
                .match(FoundLCSMessage.class, this::handle)
                .match(FoundLinearCombinationMessage.class, this::handle)
                .match(FoundHashMessage.class, this::handle)
                .match(ShutdownMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .build();

    }


    private void handle(RemoteSystemMessage message) {
        if (expectedSlaves == connectedSlaves) {
            log().warning("Number of slaves already reached. Rejecting slave");
            return;
        }

        this.log().debug("Trying to create {} worker(s) on slave", message.numberOfWorkers);
        for (int i = 0; i < message.numberOfWorkers; ++i) {
            ActorRef worker = context()
                    .actorOf(Worker.props().withDeploy(new Deploy(new RemoteScope(message.remoteAddress))));
            addWorker(worker);
            this.log().debug("Remote worker {} connected.", worker);
        }

        ++connectedSlaves;

        if (connectedSlaves == expectedSlaves) {
            log().info("All slaves connected.");
            self().tell(new Solve(), ActorRef.noSender());
        }
        else
        {
            log().info("Waiting for {} more slave(s).", expectedSlaves - connectedSlaves);
        }
    }

    /**
     * Starting message of the pipeline
     * Instructs the master to start solving the input problem. All actors have to be connected. Otherwise this message
     * will have to be resent.
     */
    public static class Solve implements Serializable { }

    private void handle(Solve message) {

        if (expectedSlaves != connectedSlaves) {
            log().debug("Cannot start problem solving pipeline yet: Waiting for {} slave(s) to connect.",
                    expectedSlaves - connectedSlaves);
            return;
        }

        final List<Routee> routees = new ArrayList<>(workers.size());

        for (ActorRef a : workers) {
            routees.add(new ActorRefRoutee(a));
        }

        router = new Router(new RoundRobinRoutingLogic(), routees);

        passwordCracker = new PasswordCracker(
                workers.size(),
                ProblemEntry.getIds(problemEntries),
                ProblemEntry.getPasswords(problemEntries));

        lcsCalculator = new LCSCalculator(ProblemEntry.getGeneSequences(problemEntries));

        hasher = new Hasher(problemEntries.size());

        log().info("Starting to solve problems.");

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 3 * workers.size(); ++i) {
            self().tell(new SolveNextSubproblemMessage(), self());
        }
    }

    private void handle(SolveNextSubproblemMessage message) {

        if (printResultsAndShutdown)
            return;

        Object nextMessage = null;
        if (lcMessagesInProgress < maxLCMessagesInProgress)
        {
            nextMessage = lcFinder.getNextSubproblem();
            if (nextMessage != null)
                ++lcMessagesInProgress;
        }

        if (nextMessage == null)
            nextMessage = passwordCracker.getNextSubproblem();

        if (nextMessage == null)
            nextMessage = lcsCalculator.getNextSubproblem();

        if (nextMessage == null)
            nextMessage = hasher.getNextSubproblem();

        if (nextMessage == null && lcFinder != null && !lcFinder.done()) {
            nextMessage = lcFinder.getNextSubproblem();
            if (nextMessage != null)
                ++lcMessagesInProgress;
        }

        if (nextMessage != null)
            router.route(nextMessage, self());
        else if (allTasksDone())
        {
            printResultsAndShutdown();
        }
    }

    private boolean allTasksDone()
    {
        return lcFinder != null && lcFinder.done() && hasher.done() && lcsCalculator.done() && passwordCracker.done();
    }

    private void handle(FoundDecryptedPasswordsMessage message) {
        if (printResultsAndShutdown)
            return;

        this.log().debug("{} sends decrypted passwords.", sender());
        passwordCracker.handle(message);

        if (passwordCracker.done() && lcFinder == null) {
            lcFinder = new LinearCombinationFinder(passwordCracker.passwords);
            maxLCMessagesInProgress = 1;
        }

        self().tell(new SolveNextSubproblemMessage(), ActorRef.noSender());
    }

    private void handle(FoundHashMessage message) {
        if (printResultsAndShutdown)
            return;

        hasher.handle(message);
        this.log().debug("{} sends hash. Missing {} one hashes and {} zero hashes.",
                sender(),
                hasher.missingOneHashes(),
                hasher.missingZeroHashes());

        self().tell(new SolveNextSubproblemMessage(), ActorRef.noSender());
    }

    private void handle(FoundLCSMessage message) {
        if (printResultsAndShutdown)
            return;

        this.log().debug("{} sends LCS.", sender());
        lcsCalculator.handle(message);

        self().tell(new SolveNextSubproblemMessage(), ActorRef.noSender());
    }

    private void handle(FoundLinearCombinationMessage message) {
        if (printResultsAndShutdown)
            return;

        this.log().debug("{} sends LC.", sender());

        lcFinder.handle(message);
        --lcMessagesInProgress;

        if (lcFinder.done() && !hasher.isPrefixesSet()) {
            hasher.setPrefixes(lcFinder.prefixes);
            maxLCMessagesInProgress = 0;
        }

        self().tell(new SolveNextSubproblemMessage(), ActorRef.noSender());
    }

    private void handle(Terminated message) {
        // Find the sender of this message
        final ActorRef sender = this.getSender();
        this.log().warning("{} has terminated.", sender);
        workers.remove(sender);
        router.removeRoutee(sender);
    }

    private void handle(ShutdownMessage message) {
        this.log().warning("Ignoring shutdown request.");
    }

    private void printResultsAndShutdown() {
        printResultsAndShutdown = true;
        printResults();
        log().info("All work done. Master is terminating itself by sending a PoisonPill.");
        self().tell(PoisonPill.getInstance(), this.getSelf());
    }

    private void printResults() {
        endTime = System.currentTimeMillis();

        try {
            // Hopefully all log entries will be printed before we print our result.
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("ID;Name;Password;Prefix;Partner;Hash");
        for (int i = 0; i < problemEntries.size(); ++i) {
            System.out.println(String.format("%d;%s;%d;%d;%d;%s",
                    problemEntries.get(i).id,
                    problemEntries.get(i).name,
                    passwordCracker.passwords[i],
                    lcFinder.prefixes[i],
                    lcsCalculator.partnerIndices[i] + 1,
                    hasher.hashes[i]));
        }
        System.out.println();
        System.out.println("Runtime password cracking [ms]: " + (passwordCracker.getEndTime() - passwordCracker.getStartTime()));
        System.out.println("Runtime linear combination [ms]: " + (lcFinder.getEndTime() - lcFinder.getStartTime()));
        System.out.println("Runtime longest common substring [ms]: " + (lcsCalculator.getEndTime() - lcsCalculator.getStartTime()));
        System.out.println("Runtime hash mining [ms]: " + (hasher.getEndTime() - hasher.getStartTime()));
        System.out.println("Total runtime [ms]: " + (endTime - startTime));
        System.out.println();
    }
}
