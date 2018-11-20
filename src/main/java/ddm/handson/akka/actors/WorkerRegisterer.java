package ddm.handson.akka.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import ddm.handson.akka.Master;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

public class WorkerRegisterer extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "workerregisterer";

    public static Props props(int numberOfWorkers) {
        return Props.create(WorkerRegisterer.class, numberOfWorkers);
    }

    public WorkerRegisterer(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    @Data
    @AllArgsConstructor
    public static class WorkerNumberMessage implements Serializable {
        private static final long serialVersionUID = -1393040810390710323L;
        public final int numberOfWorkers;
    }

    private int numberOfWorkers;

    private final Cluster cluster = Cluster.get(this.context().system());

    @Override
    public void preStart() {
        this.cluster.subscribe(this.self(), ClusterEvent.MemberUp.class);
    }

    @Override
    public void postStop() {
        this.cluster.unsubscribe(this.self());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClusterEvent.CurrentClusterState.class, this::handle)
                .match(ClusterEvent.MemberUp.class, this::handle)
                .build();
    }

    private void handle(ClusterEvent.CurrentClusterState message) {
        message.getMembers().forEach(member -> {
            if (member.status().equals(MemberStatus.up()))
                this.register(member);
        });
    }

    private void handle(ClusterEvent.MemberUp message) {
        this.register(message.member());
    }

    private void register(Member member) {
        if (member.hasRole(Master.MASTER_ROLE))
            this.getContext()
                    .actorSelection(member.address() + "/user/" + Profiler.DEFAULT_NAME)
                    .tell(new WorkerNumberMessage(this.numberOfWorkers), getSelf());
    }
}
