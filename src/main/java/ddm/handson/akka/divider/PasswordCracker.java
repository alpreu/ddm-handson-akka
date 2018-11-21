package ddm.handson.akka.divider;

import ddm.handson.akka.IdHashPair;
import ddm.handson.akka.IdPasswordPair;
import ddm.handson.akka.remote.messages.DecryptedPasswordsMessage;
import ddm.handson.akka.remote.messages.FindPasswordsMessage;

public class PasswordCracker implements ProblemDivider {

    private static final int MAX_NUMBER = 1000000;

    private final IdHashPair[] hashPairs;
    private final int stride_size;
    private int currentLowerBound;

    public final int[] passwords;
    private int passwordCount;

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
    }

    @Override
    public Object getNextSubproblem() {
        if (currentLowerBound == MAX_NUMBER)
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

    public void handle(DecryptedPasswordsMessage message) {
        if (done())
            return;

        for (IdPasswordPair p : message.passwords) {
            if (passwords[p.id - 1] > 0) {
                passwords[p.id - 1] = p.password;
                ++passwordCount;
            }
        }
    }
}
