package ddm.handson.akka.divider;

public interface ProblemDivider {
    Object getNextSubproblem();
    boolean done();
}
