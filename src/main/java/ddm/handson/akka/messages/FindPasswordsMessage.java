package ddm.handson.akka.messages;

import ddm.handson.akka.util.IdHashPair;

import java.io.Serializable;

public class FindPasswordsMessage implements Serializable {
    private static final long serialVersionUID = 8385597139269813788L;
    public final int lowerBound;
    public final int upperBound;
    public final IdHashPair[] hashes;

    @SuppressWarnings("unused")
    public FindPasswordsMessage() {
        this(0, 0, null);
    }

    public FindPasswordsMessage(int lowerBound, int upperBound,  IdHashPair[] hashes) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.hashes = hashes;
    }
}