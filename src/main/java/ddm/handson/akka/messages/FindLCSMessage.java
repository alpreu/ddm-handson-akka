package ddm.handson.akka.messages;

import java.io.Serializable;

public class FindLCSMessage implements Serializable {
    private static final long serialVersionUID = 1409540026008654655L;
    public final int indexString1;
    public final int indexString2;
    public final String string1;
    public final String string2;

    @SuppressWarnings("unused")
    public FindLCSMessage() {
        this(0, 0, "", "");
    }

    public FindLCSMessage(int indexString1, int indexString2, String string1, String string2) {
        this.indexString1 = indexString1;
        this.indexString2 = indexString2;
        this.string1 = string1;
        this.string2 = string2;
    }
}