package ddm.handson.akka.remote.divider;

import ddm.handson.akka.remote.messages.FindHashMessage;
import ddm.handson.akka.remote.messages.HashFoundMessage;

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

    private int[] prefixes;
    public String[] hashes;

    public Hasher() {
        oneHashes = new Stack<>();
        zeroHashes = new Stack<>();
        requiredOnePrefixes = Integer.MAX_VALUE;
        requiredZeroPrefixes = Integer.MAX_VALUE;
    }

    public void setPrefixes(int[] prefixes) {
        this.prefixes = prefixes;

        final int ones = (int) Arrays.stream(prefixes).filter((p) -> p == 1).count();
        requiredOnePrefixes = ones;
        requiredZeroPrefixes = prefixes.length - ones;
        hashes = new String[prefixes.length];
    }

    public void handle(HashFoundMessage message) {
        if (done())
            return;

        if (message.hash.startsWith(ONES_PREFIX))
            oneHashes.add(message.hash);
        else
            zeroHashes.add(message.hash);

        if (done()) {
            for (int i = 0; i < prefixes.length; ++i) {
                if (prefixes[i] == 1)
                    hashes[i] = oneHashes.pop();
                else
                    hashes[i] = zeroHashes.pop();
            }
        }
    }

    @Override
    public Object getNextSubproblem() {
        if (done())
            return null;

        int prefix = 0;

        if (oneHashes.size() < requiredOnePrefixes)
            prefix++;
        if (zeroHashes.size() < requiredZeroPrefixes)
            prefix--;

        return new FindHashMessage(rnd.nextInt(), prefix);
    }

    @Override
    public boolean done() {
        return oneHashes.size() >= requiredOnePrefixes && zeroHashes.size() >= requiredZeroPrefixes;
    }
}
