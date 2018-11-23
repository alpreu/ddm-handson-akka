package ddm.handson.akka.remote.messages;

import java.io.Serializable;

public class FoundLinearCombinationMessage implements Serializable {
    public final int[] solution;

    public FoundLinearCombinationMessage(int[] solution) {
        this.solution = solution;
    }
}

