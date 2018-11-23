package ddm.handson.akka.remote.divider;

import ddm.handson.akka.remote.messages.FindHashMessage;
import ddm.handson.akka.remote.messages.FoundHashMessage;
import ddm.handson.akka.util.Utils;

import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class Hasher implements ProblemDivider {

    public static final String ONES_PREFIX = "11111";
    public static final String ZERO_PREFIX = "00000";
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

    private boolean done;


    public Hasher(int maxPrefixesPerType) {
        oneHashes = new Stack<>();
        zeroHashes = new Stack<>();
        requiredOnePrefixes = maxPrefixesPerType;
        requiredZeroPrefixes = maxPrefixesPerType;
        prefixesSet = false;
        startTime = Long.MAX_VALUE;
        done = false;
    }

    public void setPrefixes(int[] prefixes) {
        this.prefixes = prefixes;

        final int ones = (int) Arrays.stream(prefixes).filter((p) -> p == 1).count();
        requiredOnePrefixes = ones;
        requiredZeroPrefixes = prefixes.length - ones;
        hashes = new String[prefixes.length];
        prefixesSet = true;
        if (done())
            writeHashes();
    }

    public void handle(FoundHashMessage message) {
        if (done())
            return;

        if (message.hash.startsWith(ONES_PREFIX))
            oneHashes.add(message.hash);
        else
            zeroHashes.add(message.hash);

        if (done()) {
            writeHashes();
        }
    }

    private void writeHashes()
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

        if (done || !additionalPrefixesRequired())
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
        if (!done)
        {
            done = isPrefixesSet() && !additionalPrefixesRequired();
        }

        return done;
    }

    public static String findHash(FindHashMessage message) {
        Random rnd = new Random(message.seed);
        String hash = Utils.hash(rnd.nextInt());

        if (message.prefix == 1) {
            while (!hash.startsWith(ONES_PREFIX)) {
                hash = Utils.hash(rnd.nextInt());
            }
        }
        else if (message.prefix == -1) {
            while (!hash.startsWith(ZERO_PREFIX)) {
                hash = Utils.hash(rnd.nextInt());
            }
        }
        else {
            while (!(hash.startsWith(ZERO_PREFIX) || hash.startsWith(ONES_PREFIX))) {
                hash = Utils.hash(rnd.nextInt());
            }
        }

        return hash;
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

    public int missingOneHashes()
    {
        return requiredOnePrefixes - oneHashes.size();
    }

    public int missingZeroHashes()
    {
        return requiredZeroPrefixes - zeroHashes.size();
    }
}
