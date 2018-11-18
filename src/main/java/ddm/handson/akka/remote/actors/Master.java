package ddm.handson.akka.remote.actors;

import akka.actor.*;
import akka.remote.RemoteScope;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import ddm.handson.akka.IdPasswordPair;
import ddm.handson.akka.ProblemEntry;
import ddm.handson.akka.remote.messages.DecryptedPasswordsMessage;
import ddm.handson.akka.remote.messages.FindPasswordsMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Master extends AbstractLoggingActor {

    public static class Solve implements Serializable {
    }

    public static class RemoteSystemMessage {
        public final Address remoteAddress;
        public final int numberOfWorkers;

        public RemoteSystemMessage(Address remoteAddress, int numberOfWorkers) {
            this.remoteAddress = remoteAddress;
            this.numberOfWorkers = numberOfWorkers;
        }
    }

    public static class FindLinearCombinationMessage implements Serializable{

    }

    public static final String DEFAULT_NAME = "master";

    private final ActorRef listener;

    private final List<ProblemEntry> problemEntries;

    private final int expectedSlaves;
    private int connectedSlaves;
    private final HashSet<ActorRef> workers;

    private int passwordsDecrypted;
    private final int[] decryptedPasswords;
    private long startTime;


    private Router router;

    public Master(final ActorRef listener, int numberOfWorkers, int expectedSlaves, final List<ProblemEntry> problemEntries) {
        this.listener = listener;
        this.expectedSlaves = expectedSlaves;
        connectedSlaves = 0;

        workers = new HashSet<>(42);

        for (int i = 0; i < numberOfWorkers; ++i) {
            final ActorRef worker = getContext().actorOf(Worker.props());
            this.getContext().watch(worker);
            workers.add(worker);
        }

        // Set up variables
        this.problemEntries = problemEntries;
        decryptedPasswords = new int[problemEntries.size()];
        passwordsDecrypted = 0;
        router = null;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log().info("Master started");
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Terminated.class, this::handle)
                .match(Solve.class, this::handle)
                .match(DecryptedPasswordsMessage.class, this::handle)
                .match(RemoteSystemMessage.class, this::handle)
                .match(FindLinearCombinationMessage.class, this::handle)
                .match(Worker.LinearCombinationSolutionMessage.class, this::handle)
                .build();

    }


    private void handle(Worker.LinearCombinationSolutionMessage message) {
        if (message.solution == -1)
            return;

        this.log().info("Subset Sum solved in {} ms.", System.currentTimeMillis() - startTime);

        int[] pwds = Arrays.copyOf(decryptedPasswords, decryptedPasswords.length);
        Arrays.sort(pwds);

        for (int i = 0; i < decryptedPasswords.length; ++i) {
            int posInPwds = indexOf(pwds, decryptedPasswords[i]);
            if ((message.solution & (1l << posInPwds)) != 0) {
                this.log().info("id: {} prefix: 1", i + 1);
            }
            else
                this.log().info("id: {} prefix: -1", i + 1);
        }
    }
    private static int indexOf(int[] array, int value)
    {
        for (int i = 0; i < array.length; ++i)
        {
            if (array[i] == value)
                return i;
        }

        return -1;
    }

    private void handle(FindLinearCombinationMessage message) {
        this.log().info("Solving Subset Sum");
        startTime = System.currentTimeMillis();

        int[] pwds = Arrays.copyOf(decryptedPasswords, decryptedPasswords.length);
        Arrays.sort(pwds);

        int sum = 0;
        for ( int i : pwds)
            sum += i;

        sum /= 2;

        for (int i = 0; i < workers.size(); ++i)
        {
            router.route(new Worker.FindLinearCombinationMessage(i, (byte)Math.ceil(Math.log(i)), sum, pwds), self());
        }
    }



    private void handle(RemoteSystemMessage message) {
        if (expectedSlaves == connectedSlaves) {
            log().warning("Number of slaves already reached. Rejecting slave");
            return;
        }

        this.log().info("Trying to create workers on slave");
        for (int i = 0; i < message.numberOfWorkers; ++i) {
            ActorRef worker = context()
                    .actorOf(Worker.props().withDeploy(new Deploy(new RemoteScope(message.remoteAddress))));
            context().watch(worker);
            workers.add(worker);
            this.log().info("Remote worker {} connected.", worker);
        }

        ++connectedSlaves;

        if (connectedSlaves == expectedSlaves) {
            log().info("All slaves connected.");
            self().tell(new Solve(), ActorRef.noSender());
        }
        else
        {
            log().info("Waiting for {} more slaves.", expectedSlaves - connectedSlaves);
        }
    }

    private void handle(Terminated message) {
        final ActorRef sender = this.getSender();
        workers.remove(sender);
        this.log().warning("Worker {} terminated.", sender);

        if (hasFinished()) {
            stopSelfAndListener();
        }
    }

    private void handle(DecryptedPasswordsMessage message) {
        this.log().info("Passwords received.");
        for (IdPasswordPair p : message.passwords) {
            decryptedPasswords[p.id - 1] = p.Password;
            ++passwordsDecrypted;
        }
        if (passwordsDecrypted == problemEntries.size()) {
            log().info("Passowords cracked in {} ms", System.currentTimeMillis() - startTime);
            for (int id = 1; id <= problemEntries.size(); ++id) {
                this.log().info("id: {} pwd: {}", id, decryptedPasswords[id - 1]);
            }
            self().tell(new FindLinearCombinationMessage(), ActorRef.noSender());
        }
    }

    private void handle(Solve message) {

        if (expectedSlaves != connectedSlaves) {
            log().info("Waiting for {} slaves",
                    expectedSlaves - connectedSlaves);
            return;
        }

        List<Routee> routees = new ArrayList<>(workers.size());

        for (ActorRef a : workers) {
            routees.add(new ActorRefRoutee(a));
        }

        router = new Router(new RoundRobinRoutingLogic(), routees);

        // 1. Knacke die Kennwörter
        startTime = System.currentTimeMillis();
        final int maxNumber = 1000000;
        final int stride_size = Math.max(maxNumber / workers.size(), 1);

        for (int i = 0; i < maxNumber; i += stride_size) {
            router.route(new FindPasswordsMessage(i, Math.min(i + stride_size, maxNumber) - 1, problemEntries), self());
        }


        // 2. Finde LC
        // 3. Finde LCS
        // 4. ??
    }

    private boolean hasFinished() {
        return false;
    }

    private void stopSelfAndListener() {
        // Work is done. We don't need the listener anymore
        //this.listener.tell(new ShutdownMessage(), this.getSelf());

        // Stop self and all child actors by sending a poison pill.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log().info("Sending PosionPill to Master");
        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());
    }

    public static Props props(ActorRef listener, int numberOfWorkers, int numberOfSlaves, final List<ProblemEntry> problemEntries) {
        return Props.create(Master.class, listener, numberOfWorkers, numberOfSlaves, problemEntries);
    }


}
