package ddm.handson.akka.remote.actors;


import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import ddm.handson.akka.remote.divider.Hasher;
import ddm.handson.akka.remote.divider.LCSCalculator;
import ddm.handson.akka.remote.divider.LinearCombinationFinder;
import ddm.handson.akka.remote.messages.*;
import ddm.handson.akka.util.IdHashPair;
import ddm.handson.akka.util.IdPasswordPair;
import ddm.handson.akka.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Worker extends AbstractLoggingActor {

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

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FindPasswordsMessage.class, this::handle)
                .match(FindLinearCombinationMessage.class, this::handle)
                .match(FindLCSMessage.class, this::handle)
                .match(FindHashMessage.class, this::handle)
                .matchAny(object -> System.out.println("Unknown message"))
                .build();
    }

    @SuppressWarnings("Duplicates")
    private void handle(FindPasswordsMessage message) {
        HashMap<String, Integer> hashes = new HashMap<>(message.upperBound - message.lowerBound + 1);
        for (int i = message.lowerBound; i <= message.upperBound; ++i) {
            final String hash = Utils.hash(i);
            hashes.put(hash, i);
            if (hash.startsWith(Hasher.ONES_PREFIX) || hash.startsWith(Hasher.ZERO_PREFIX))
                sender().tell(new FoundHashMessage(hash), self());
        }

        List<IdPasswordPair> results = new ArrayList<>(message.hashes.length);

        for (IdHashPair pair : message.hashes) {
            int password = hashes.getOrDefault(pair.hash, Integer.MAX_VALUE);
            if (password < Integer.MAX_VALUE) {
                results.add(new IdPasswordPair(pair.id, password));
            }
        }
        sender().tell(new FoundDecryptedPasswordsMessage(results.toArray(new IdPasswordPair[0])), self());
    }

    private void handle(FindLinearCombinationMessage message) {
        final int[] solution = LinearCombinationFinder.handle(message);
        sender().tell(new FoundLinearCombinationMessage(solution), self());
    }

    private void handle(FindLCSMessage message) {
        int max = LCSCalculator.CalcLCS(message);
        sender().tell(new FoundLCSMessage(message.indexString1, message.indexString2, max), self());
    }

    private void handle(FindHashMessage message) {
        sender().tell(new FoundHashMessage(Hasher.findHash(message)), self());
    }
}
