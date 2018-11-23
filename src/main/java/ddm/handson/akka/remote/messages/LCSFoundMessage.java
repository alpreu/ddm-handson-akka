package ddm.handson.akka.remote.messages;

import java.io.Serializable;

public class LCSFoundMessage implements Serializable {
    public final int indexString1;
    public final int indexString2;
    public final int lcsLength;

    public LCSFoundMessage(int indexString1, int indexString2, int lcsLength) {
        this.indexString1 = indexString1;
        this.indexString2 = indexString2;
        this.lcsLength = lcsLength;
    }
}