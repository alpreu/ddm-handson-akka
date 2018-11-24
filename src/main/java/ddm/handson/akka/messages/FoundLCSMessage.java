package ddm.handson.akka.messages;

import java.io.Serializable;

public class FoundLCSMessage implements Serializable {
    public final int indexString1;
    public final int indexString2;
    public final int lcsLength;

    @SuppressWarnings("unused")
    public FoundLCSMessage() {
        this(0, 0, 0);
    }

    public FoundLCSMessage(int indexString1, int indexString2, int lcsLength) {
        this.indexString1 = indexString1;
        this.indexString2 = indexString2;
        this.lcsLength = lcsLength;
    }
}