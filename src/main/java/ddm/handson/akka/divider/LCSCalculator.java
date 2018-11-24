package ddm.handson.akka.divider;

import ddm.handson.akka.messages.FindLCSMessage;
import ddm.handson.akka.messages.FoundLCSMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class LCSCalculator implements ProblemDivider {

    private static class LCSPair
    {
        public final int indexString1;
        public final int indexString2;
        public final int lcs;

        private LCSPair(int indexString1, int indexString2, int lcs) {
            this.indexString1 = indexString1;
            this.indexString2 = indexString2;
            this.lcs = lcs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LCSPair lcsPair = (LCSPair) o;
            return indexString1 == lcsPair.indexString1 &&
                    indexString2 == lcsPair.indexString2 &&
                    lcs == lcsPair.lcs;
        }

        @Override
        public int hashCode() {

            return Objects.hash(indexString1, indexString2, lcs);
        }
    }


    private final String[] strings;
    public final int[] partnerIndices;

    private int indexString1;
    private int indexString2;

    private final int pairCount;
    private HashSet<LCSPair> lcsPairs;

    private long startTime;
    private long endTime;

    public LCSCalculator(String[] strings) {
        this.strings = strings;
        indexString1 = 0;
        indexString2 = 0;

        partnerIndices = new int[strings.length];
        Arrays.fill(partnerIndices, -1);
        pairCount = ((strings.length - 1) * strings.length) / 2;
        lcsPairs = new HashSet<>(pairCount);

        startTime = Long.MAX_VALUE;
    }


    @Override
    public Object getNextSubproblem() {
        if (startTime == Long.MAX_VALUE)
            startTime = System.currentTimeMillis();

        if (done())
            return null;

        ++indexString2;
        if (indexString2 == strings.length) {
            ++indexString1;
            indexString2 = indexString1 + 1;
        }

        if (indexString2 >= strings.length || indexString1 >= strings.length) {
            return null;
        }

        return new FindLCSMessage(indexString1, indexString2, strings[indexString1], strings[indexString2]);
    }

    @Override
    public boolean done() {
        return lcsPairs.size() == pairCount;
    }

    public void handle(FoundLCSMessage message) {
        if (done())
            return;

        lcsPairs.add(new LCSPair(message.indexString1, message.indexString2, message.lcsLength));

        if (done()) {
            endTime = System.currentTimeMillis();
            final int lcs[] = new int[strings.length];
            lcsPairs.stream().forEach((item) -> {
                if (item.lcs > lcs[item.indexString1]) {
                    partnerIndices[item.indexString1] = item.indexString2;
                    partnerIndices[item.indexString2] = item.indexString1;
                    lcs[item.indexString1] = item.lcs;
                    lcs[item.indexString2] = item.lcs;
                }
            });
        }
    }

    public static int CalcLCS(FindLCSMessage message) {
        int[][] matrix = new int[message.string1.length() + 1][message.string2.length() + 1];
        int max = 0;

        for (int i = 1; i <= message.string1.length(); ++i) {
            for (int j = 1; j <= message.string2.length(); ++j) {
                if (message.string1.charAt(i - 1) == message.string2.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                    max = Math.max(max, matrix[i][j]);
                }
            }
        }
        return max;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}