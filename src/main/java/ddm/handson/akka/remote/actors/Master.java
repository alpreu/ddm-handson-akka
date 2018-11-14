package ddm.handson.akka.remote.actors;

import akka.actor.*;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import ddm.handson.akka.IdPasswordPair;
import ddm.handson.akka.ProblemEntry;
import ddm.handson.akka.remote.messages.DecryptedPasswordsMessage;
import ddm.handson.akka.remote.messages.FindPasswordsMessage;
import ddm.handson.akka.remote.messages.ShutdownMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Master extends AbstractLoggingActor {

    public static class Solve implements Serializable { }

    public static final String DEFAULT_NAME = "master";

    private final ActorRef listener;

    private final List<ProblemEntry> problemEntries;

    private final Router router;

    private int passwordsDecrypted;
    private final int[] decryptedPasswords;


    public Master(final ActorRef listener, final int numberOfWorkers, final List<ProblemEntry> problemEntries)
    {
        this.listener = listener;
        List<Routee> routees = new ArrayList<>(256);

        for (int i = 0; i < numberOfWorkers; ++i)
        {
            final ActorRef worker = getContext().actorOf(Worker.props());
            this.getContext().watch(worker);
            routees.add(new ActorRefRoutee(worker));

        }

        this.router = new Router(new RoundRobinRoutingLogic(), routees);

        this.problemEntries = problemEntries;
        decryptedPasswords = new int[problemEntries.size()];
        passwordsDecrypted = 0;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Terminated.class, this::handle)
                .match(Solve.class, this::handle)
                .match(DecryptedPasswordsMessage.class, this::handle)
                .build();

    }

    private void handle(Terminated message) {
        final ActorRef sender = this.getSender();
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
            for (int id = 1; id <= problemEntries.size(); ++id) {
                this.log().info("id: {} pwd: {}", id, decryptedPasswords[id - 1]);
            }
        }
    }

    private void handle(Solve message) {
        // 1. Knacke die Kennwörter
        final int maxNumber = 1000000;
        final int stride_size = maxNumber / 4;

        for (int i = 0; i< maxNumber; i += stride_size) {
            router.route(new FindPasswordsMessage(i, Math.min(i + stride_size, maxNumber) - 1, problemEntries), self());
        }



        // 2. Finde LC
        // 3. Finde LCS
        // 4. ??
    }

    private boolean hasFinished() {
        return true;
    }

    private void stopSelfAndListener() {
        // Work is done. We don't need the listener anymore
        this.listener.tell(new ShutdownMessage(), this.getSelf());

        // Stop self and all child actors by sending a poison pill.
        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());
    }

    public static Props props(ActorRef listener, final int numberOfWorkers, final List<ProblemEntry> problemEntries)
    {
        return Props.create(Master.class, listener, numberOfWorkers, problemEntries);
    }
}
