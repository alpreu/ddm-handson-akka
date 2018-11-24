package ddm.handson.akka.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import ddm.handson.akka.MasterActorSystem;
import ddm.handson.akka.divider.Hasher;
import ddm.handson.akka.divider.LCSCalculator;
import ddm.handson.akka.divider.LinearCombinationFinder;
import ddm.handson.akka.messages.*;
import ddm.handson.akka.util.IdHashPair;
import ddm.handson.akka.util.IdPasswordPair;
import ddm.handson.akka.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Worker extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "worker";

    public static Props props() {
        return Props.create(Worker.class);
    }

    private final Cluster cluster = Cluster.get(this.context().system());

    @Override
    public void preStart() {
        this.cluster.subscribe(this.self(), MemberUp.class);
    }

    @Override
    public void postStop() {
        this.cluster.unsubscribe(this.self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CurrentClusterState.class, this::handle)
                .match(MemberUp.class, this::handle)
                .match(FindPasswordsMessage.class, this::handle)
                .match(FindLinearCombinationMessage.class, this::handle)
                .match(FindLCSMessage.class, this::handle)
                .match(FindHashMessage.class, this::handle)
                .matchAny(o -> this.log().info("Received unknown message: {}", o.toString()))
                .build();
    }

    private void handle(CurrentClusterState message) {
        message.getMembers().forEach(member -> {
            if (member.status().equals(MemberStatus.up()))
                this.register(member);
        });
    }

    private void handle(MemberUp message) {
        this.register(message.member());
    }

    private void register(Member member) {
        if (member.hasRole(MasterActorSystem.MASTER_ROLE))
            this.getContext()
                    .actorSelection(member.address() + "/user/" + Master.DEFAULT_NAME)
                    .tell(new WorkerRegistrationMessage(), this.self());
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
