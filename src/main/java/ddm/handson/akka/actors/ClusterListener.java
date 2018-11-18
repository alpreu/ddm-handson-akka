package ddm.handson.akka.actors;


import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.CurrentClusterState;



public class ClusterListener extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "clusterListener";

    public static Props props() {
        return Props.create(ClusterListener.class);
    }
    private final Cluster cluster = Cluster.get(this.context().system());

    //subscribe to cluster changes
    @Override
    public void preStart() {
        this.cluster.subscribe(this.self(), MemberEvent.class, UnreachableMember.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        this.cluster.unsubscribe(this.self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CurrentClusterState.class, state -> this.log().info("Current members: {}", state.members()))
                .match(MemberUp.class, mUp -> this.log().info("Member is Up: {}", mUp.member()))
                .match(UnreachableMember.class, mUnreachable -> this.log().info("Member detected as unreachable: {}", mUnreachable.member()))
                .match(MemberRemoved.class, mRemoved -> this.log().info("Member is Removed: {}", mRemoved.member()))
                .match(MemberEvent.class, message -> {}) //ignore
                .build();
    }
}
