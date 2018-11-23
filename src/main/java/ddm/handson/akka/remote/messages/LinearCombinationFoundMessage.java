package ddm.handson.akka.remote.messages;

import java.io.Serializable;

public class LinearCombinationFoundMessage implements Serializable {
    public final int[] solution;

    public LinearCombinationFoundMessage(int[] solution) {
        this.solution = solution;
    }
}

