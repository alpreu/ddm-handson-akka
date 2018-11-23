package ddm.handson.akka.remote.divider;

import ddm.handson.akka.util.IdHashPair;
import ddm.handson.akka.util.IdPasswordPair;
import ddm.handson.akka.remote.messages.FoundDecryptedPasswordsMessage;
import ddm.handson.akka.remote.messages.FindPasswordsMessage;
import ddm.handson.akka.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PasswordCracker implements ProblemDivider {

    private static final int MAX_NUMBER = 1000000;

    private final IdHashPair[] hashPairs;
    private final int stride_size;
    private int currentLowerBound;

    public final int[] passwords;
    private int passwordCount;

    private long startTime;
    private long endTime;

    public PasswordCracker(int numberOfWorkers, int[] ids, String[] hashes)
    {
        currentLowerBound = 100000;
        stride_size = Math.max((MAX_NUMBER - currentLowerBound) / numberOfWorkers, 1);
        passwords = new int[ids.length];
        passwordCount = 0;

        hashPairs = new IdHashPair[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            hashPairs[i] = new IdHashPair(ids[i], hashes[i]);
        }

        startTime = Long.MAX_VALUE;
    }

    @Override
    public Object getNextSubproblem() {
        if (startTime == Long.MAX_VALUE)
            startTime = System.currentTimeMillis();

        if (currentLowerBound == MAX_NUMBER)
            return null;
        if (done())
            return null;

        final int upperBound = Math.min(currentLowerBound + stride_size, MAX_NUMBER) - 1;
        Object message = new FindPasswordsMessage(currentLowerBound, upperBound, hashPairs);
        currentLowerBound = upperBound + 1;
        return message;
    }

    @Override
    public boolean done() {
        return passwordCount == passwords.length;
    }

    public void handle(FoundDecryptedPasswordsMessage message) {
        if (done())
            return;

        for (IdPasswordPair p : message.passwords) {
            if (passwords[p.id - 1] == 0) {
                passwords[p.id - 1] = p.password;
                ++passwordCount;
            }
        }

        if (done())
            endTime = System.currentTimeMillis();
    }

    public static IdPasswordPair[] FindPasswords(FindPasswordsMessage message)
    {
        HashMap<String, Integer> hashes = new HashMap<>(message.upperBound - message.lowerBound + 1);
        for (int i = message.lowerBound; i <= message.upperBound; ++i) {
            hashes.put(Utils.hash(i), i);
        }

        List<IdPasswordPair> results = new ArrayList<>(message.hashes.length);

        for (IdHashPair pair : message.hashes) {
            int password = hashes.getOrDefault(pair.hash, Integer.MAX_VALUE);
            if (password < Integer.MAX_VALUE) {
                results.add(new IdPasswordPair(pair.id, password));
            }
        }
        return results.toArray(new IdPasswordPair[results.size()]);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
