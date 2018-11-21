package ddm.handson.akka.remote.messages;

import ddm.handson.akka.util.IdHashPair;

import java.io.Serializable;

public class FindPasswordsMessage implements Serializable {
    public final int lowerBound;
    public final int upperBound;
    public final IdHashPair[] hashes;

    public FindPasswordsMessage(int lowerBound, int upperBound,  IdHashPair[] hashes) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.hashes = hashes;
    }
}
