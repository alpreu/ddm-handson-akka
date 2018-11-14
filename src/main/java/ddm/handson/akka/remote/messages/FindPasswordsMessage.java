package ddm.handson.akka.remote.messages;

import ddm.handson.akka.IdPasswordPair;
import ddm.handson.akka.ProblemEntry;

import java.io.Serializable;
import java.util.List;

public class FindPasswordsMessage implements Serializable {
    public final int lowerBound;
    public final int upperBound;
    public final List<ProblemEntry> problemEntries;

    public FindPasswordsMessage(int lowerBound, int upperBound, List<ProblemEntry> problemEntries) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.problemEntries = problemEntries;
    }
}
