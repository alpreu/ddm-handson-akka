package ddm.handson.akka.messages;

import java.io.Serializable;

public class FoundLinearCombinationMessage implements Serializable {
    private static final long serialVersionUID = 9070433995767806206L;
    public final int[] solution;

    @SuppressWarnings("unused")
    public FoundLinearCombinationMessage() {
        this(null);
    }

    public FoundLinearCombinationMessage(int[] solution) {
        this.solution = solution;
    }
}