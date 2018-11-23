package ddm.handson.akka.remote.divider;

import ddm.handson.akka.remote.messages.FindHashMessage;
import ddm.handson.akka.remote.messages.FoundHashMessage;

import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class Hasher implements ProblemDivider {

    private static final String ONES_PREFIX = "11111";
    private static final String ZERO_PREFIX = "00000";
    private static final Random rnd = new Random();

    private final Stack<String> oneHashes;
    private final Stack<String> zeroHashes;

    private int requiredOnePrefixes;
    private int requiredZeroPrefixes;

    private boolean prefixesSet;
    private int[] prefixes;
    public String[] hashes;

    private long startTime;
    private long endTime;


    public Hasher(int maxPrefixesPerType) {
        oneHashes = new Stack<>();
        zeroHashes = new Stack<>();
        requiredOnePrefixes = maxPrefixesPerType;
        requiredZeroPrefixes = maxPrefixesPerType;
        prefixesSet = false;
        startTime = Long.MAX_VALUE;
    }

    public void setPrefixes(int[] prefixes) {
        this.prefixes = prefixes;

        final int ones = (int) Arrays.stream(prefixes).filter((p) -> p == 1).count();
        requiredOnePrefixes = ones;
        requiredZeroPrefixes = prefixes.length - ones;
        hashes = new String[prefixes.length];
        prefixesSet = true;
        if (done())
            writePrefixes();
    }

    public void handle(FoundHashMessage message) {
        if (done())
            return;

        if (message.hash.startsWith(ONES_PREFIX))
            oneHashes.add(message.hash);
        else
            zeroHashes.add(message.hash);

        if (done()) {
            writePrefixes();
        }
    }

    private void writePrefixes()
    {
        endTime = System.currentTimeMillis();
        for (int i = 0; i < prefixes.length; ++i) {
            if (prefixes[i] == 1)
                hashes[i] = oneHashes.pop();
            else
                hashes[i] = zeroHashes.pop();
        }
    }

    @Override
    public Object getNextSubproblem() {
        if (startTime == Long.MAX_VALUE)
            startTime = System.currentTimeMillis();

        if (!additionalPrefixesRequired())
            return null;

        int prefix = 0;

        if (oneHashes.size() < requiredOnePrefixes)
            prefix++;
        if (zeroHashes.size() < requiredZeroPrefixes)
            prefix--;

        return new FindHashMessage(rnd.nextInt(), prefix);
    }

    private boolean additionalPrefixesRequired()
    {
        return !(oneHashes.size() >= requiredOnePrefixes && zeroHashes.size() >= requiredZeroPrefixes);
    }

    @Override
    public boolean done() {
        return isPrefixesSet() && additionalPrefixesRequired();
    }

    public boolean isPrefixesSet() {
        return prefixesSet;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
