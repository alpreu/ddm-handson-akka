package ddm.handson.akka.remote.messages;

import java.io.Serializable;

public class FoundLinearCombinationMessage implements Serializable {
    public final int[] solution;

    @SuppressWarnings("unused")
    public FoundLinearCombinationMessage() {
        this(null);
    }

    public FoundLinearCombinationMessage(int[] solution) {
        this.solution = solution;
    }
}

