package ddm.handson.akka.remote.divider;

public interface ProblemDivider {
    Object getNextSubproblem();
    boolean done();
}
