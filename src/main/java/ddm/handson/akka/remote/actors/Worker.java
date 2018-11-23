package ddm.handson.akka.remote.actors;


import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import ddm.handson.akka.remote.divider.LCSCalculator;
import ddm.handson.akka.remote.divider.LinearCombinationFinder;
import ddm.handson.akka.remote.divider.PasswordCracker;
import ddm.handson.akka.remote.messages.*;
import ddm.handson.akka.util.Utils;

import java.io.Serializable;
import java.util.Random;

public class Worker extends AbstractLoggingActor {
//region messages



//endregion
    @Override
    public Receive createReceive() {
        System.out.println("createReceive called.");
        return receiveBuilder()
                .match(FindPasswordsMessage.class, this::handle)
                .match(FindLinearCombinationMessage.class, this::handle)
                .match(FindLCSMessage.class, this::handle)
                .match(FindHashesMessage.class, this::handle)
                .matchAny(object -> System.out.println("Unknown message"))
                .build();
    }

    private void handle(FindHashesMessage message) {
        Random rnd = new Random(message.seed);

        final String prefix = message.ones ? "11111" : "00000";
        String hash;
        int nonce;

        do {
            nonce = rnd.nextInt();
            hash = Utils.hash(nonce + message.partnerId);
        } while (!hash.startsWith(prefix));

        sender().tell(new Master.HashFoundMessage(message.id, hash), self());
    }

    private void handle(FindLinearCombinationMessage message) {
        final int[] solution = LinearCombinationFinder.handle(message);
        sender().tell(new LinearCombinationFoundMessage(solution), self());
    }

    public static Props props()
    {
        return Props.create(Worker.class);
    }


    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log().info("Worker {} shuttig down", self());
    }

    private void handle(FindPasswordsMessage message) {
        this.getSender().tell(new DecryptedPasswordsMessage(PasswordCracker.FindPasswords(message)), self());
    }

    private void handle(FindLCSMessage message) {
        int max = LCSCalculator.CalcLCS(message);
        sender().tell(new LCSFoundMessage(message.indexString1, message.indexString2, max), self());
    }
}
