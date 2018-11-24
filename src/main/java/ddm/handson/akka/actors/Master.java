package ddm.handson.akka.actors;

import akka.actor.*;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import ddm.handson.akka.divider.Hasher;
import ddm.handson.akka.divider.LCSCalculator;
import ddm.handson.akka.divider.LinearCombinationFinder;
import ddm.handson.akka.divider.PasswordCracker;
import ddm.handson.akka.messages.*;
import ddm.handson.akka.util.ProblemEntry;

import java.io.Serializable;
import java.util.*;

public class Master extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "master";
    private static final int workerRegistrationsInitialValue = Integer.MAX_VALUE;


    private int numberOfWorkerSystems;
    private int systemRegistrationCount = 0;
    private int requiredWorkerRegistrations = workerRegistrationsInitialValue;
    private int workerRegistrations = 0;
    private final HashSet<ActorRef> workers = new HashSet<>();
    private final List<ProblemEntry> problemEntries;
    private Router router;
    private PasswordCracker passwordCracker;
    private LCSCalculator lcsCalculator;
    private Hasher hasher;
    private LinearCombinationFinder lcFinder;
    private int lcMessagesInProgress;
    private int maxLCMessagesInProgress;
    private boolean printResultsAndShutdown;
    private long startTime;
    private long endTime;


    public static Props props(int numberOfWorkerSystems, List<ProblemEntry> problemEntries) {
        return Props.create(Master.class, numberOfWorkerSystems, problemEntries);
    }

    public Master(int numberOfWorkerSystems, List<ProblemEntry> problemEntries) {
        this.numberOfWorkerSystems = numberOfWorkerSystems;
        this.problemEntries = problemEntries;

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WorkerNumberMessage.class, this::handle)
                .match(WorkerRegistrationMessage.class, this::handle)
                .match(Solve.class, this::handle)
                .match(SolveNextSubproblemMessage.class, this::handle)
                .match(FoundDecryptedPasswordsMessage.class, this::handle)
                .match(FoundLCSMessage.class, this::handle)
                .match(FoundLinearCombinationMessage.class, this::handle)
                .match(FoundHashMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .matchAny(object -> this.log().info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }


    private void handle(WorkerNumberMessage message) {
        systemRegistrationCount++;
        if (requiredWorkerRegistrations == workerRegistrationsInitialValue) {
            requiredWorkerRegistrations = message.numberOfWorkers; //replace placeholder value
        } else {
            requiredWorkerRegistrations += message.numberOfWorkers; //just add
        }
    }

    private void handle(WorkerRegistrationMessage message) {
        workerRegistrations++;
        this.context().watch(this.sender());
        this.workers.add(this.sender());

        this.log().info("Registered {}", this.sender());

        if (allWorkersRegistered()) {
            self().tell(new Solve(), self());
        }
    }

    private void handle(Terminated message) {
        this.context().unwatch(message.getActor());
        this.workers.remove(message.getActor());
        this.log().info("Unregistered {}", message.getActor());
    }




    public static class Solve implements Serializable { }

    private void handle(Solve message) {
        if (!allWorkersRegistered()) {
            System.out.println("cannot start solving, not all workers or systems have registered");
            return;
        }

        final List<Routee> routees = new ArrayList<>(workers.size());
        for (ActorRef a: workers) {
            routees.add(new ActorRefRoutee(a));
        }

        router = new Router(new SmallestMailboxRoutingLogic(), routees);

        passwordCracker = new PasswordCracker(
                workers.size(),
                ProblemEntry.getIds(problemEntries),
                ProblemEntry.getPasswords(problemEntries)
        );

        lcsCalculator = new LCSCalculator(ProblemEntry.getGeneSequences(problemEntries));

        hasher = new Hasher(problemEntries.size());

        log().info("starting to solve problems.");

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 10 * workers.size(); i++) {
            self().tell(new SolveNextSubproblemMessage(), self());
        }

    }

    private boolean allSystemsRegistered() {
        return systemRegistrationCount == numberOfWorkerSystems;
    }

    private boolean allWorkersRegistered() {
        return allSystemsRegistered() && (workerRegistrations == requiredWorkerRegistrations);
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

        this.log().debug("Decrypted passwords received.");
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
        this.log().debug("Hash received. Missing {} one hashes and {} zero hashes.",
                hasher.missingOneHashes(),
                hasher.missingZeroHashes());

        self().tell(new SolveNextSubproblemMessage(), ActorRef.noSender());
    }

    private void handle(FoundLCSMessage message) {
        if (printResultsAndShutdown)
            return;

        this.log().debug("Longest common substring received.");
        lcsCalculator.handle(message);

        self().tell(new SolveNextSubproblemMessage(), ActorRef.noSender());
    }

    private void handle(FoundLinearCombinationMessage message) {
        if (printResultsAndShutdown)
            return;

        this.log().debug("Linear combination received. (Timeout is possible.)");

        lcFinder.handle(message);
        --lcMessagesInProgress;

        if (lcFinder.done() && !hasher.isPrefixesSet()) {
            hasher.setPrefixes(lcFinder.prefixes);
            maxLCMessagesInProgress = 0;
        }

        self().tell(new SolveNextSubproblemMessage(), ActorRef.noSender());
    }

    private void printResultsAndShutdown() {
        printResultsAndShutdown = true;
        printResults();
        log().info("All work done. MasterActorSystem is terminating itself by sending a PoisonPill.");
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
