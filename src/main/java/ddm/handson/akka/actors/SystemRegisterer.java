package ddm.handson.akka.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import ddm.handson.akka.MasterActorSystem;
import ddm.handson.akka.messages.WorkerNumberMessage;

public class SystemRegisterer extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "workerregisterer";

    public static Props props(int numberOfWorkers) {
        return Props.create(SystemRegisterer.class, numberOfWorkers);
    }

    public SystemRegisterer(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
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
        if (member.hasRole(MasterActorSystem.MASTER_ROLE))
            this.getContext()
                    .actorSelection(member.address() + "/user/" + Master.DEFAULT_NAME)
                    .tell(new WorkerNumberMessage(this.numberOfWorkers), getSelf());
    }
}
