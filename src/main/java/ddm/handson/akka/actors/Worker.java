package ddm.handson.akka.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import ddm.handson.akka.Master;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

public class Worker extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "worker";

    public static Props props() {
        return Props.create(Worker.class);
    }

    @Data @AllArgsConstructor
    public static class WorkMessage implements Serializable {
        private static final long serialVersionUID = -7643194361832862395L;
        private WorkMessage() {}
        private String content;
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
                .match(WorkMessage.class, this::handle)
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

    private void handle(WorkMessage message) {
        System.out.println("Received work message: " + message.content);
    }

    private void register(Member member) {
        //TODO
        System.out.println(this.self().path() + " saw a member register at " + member.address());
    }
}
