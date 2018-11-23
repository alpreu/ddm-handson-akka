package ddm.handson.akka.remote.divider;

import ddm.handson.akka.remote.messages.FindLCSMessage;
import ddm.handson.akka.remote.messages.LCSFoundMessage;

import java.util.*;

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
    public final int[] partnerIds;

    private int indexString1;
    private int indexString2;

    private final int pairCount;
    private HashSet<LCSPair> lcsPairs;

    public LCSCalculator(String[] strings) {
        this.strings = strings;
        indexString1 = 0;
        indexString2 = 0;

        partnerIds = new int[strings.length];
        Arrays.fill(partnerIds, -1);
        pairCount = ((strings.length - 1) * strings.length) / 2;
        lcsPairs = new HashSet<>(pairCount);
    }


    @Override
    public Object getNextSubproblem() {

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

    public void handle(LCSFoundMessage message) {
        if (done())
            return;

        lcsPairs.add(new LCSPair(message.indexString1, message.indexString1, message.lcsLength));

        if (done()) {
            final int lcs[] = new int[strings.length];
            lcsPairs.stream().forEach((item) -> {
                if (item.lcs > lcs[item.indexString1]) {
                    partnerIds[item.indexString1] = item.indexString2;
                    partnerIds[item.indexString2] = item.indexString1;
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
}
