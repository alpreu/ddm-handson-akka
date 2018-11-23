package ddm.handson.akka.remote.divider;

import ddm.handson.akka.remote.messages.FindLinearCombinationMessage;
import ddm.handson.akka.remote.messages.FoundLinearCombinationMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class LinearCombinationFinder implements ProblemDivider {

    public final int[] prefixes;

    private final int[] originalPasswordArray;
    private final int[] passwords;
    private final int halfPasswordsSum;
    private boolean done;

    private long prefix;

    private long startTime;
    private long endTime;

    public LinearCombinationFinder(int[] passwords) {
        originalPasswordArray = passwords;
        this.passwords = passwords.clone();
        Arrays.sort(this.passwords);

        int sum = 0;
        for (int p : passwords)
            sum += p;
        halfPasswordsSum = sum / 2;
        done = passwords.length <= 1;

        prefixes = new int[passwords.length];
        prefix = -2;

        startTime = Long.MAX_VALUE;
    }

    @Override
    public Object getNextSubproblem() {
        if (startTime == Long.MAX_VALUE)
            startTime = System.currentTimeMillis();

        if (done())
            return null;

        int prefixLength;

        ++prefix;

        if (prefix == -1) {
            prefixLength = 0;
        }
        else if (prefix == 0) {
            prefixLength = 1;
        }
        else {
            prefixLength = (int)(Math.log(prefix) + 0.5);
        }

        return new FindLinearCombinationMessage(prefix, prefixLength, halfPasswordsSum, passwords);
    }

    @Override
    public boolean done() {
        return done;
    }

    public void handle(FoundLinearCombinationMessage message) {
        if (done())
            return;

        if (message.solution.length == 0)
            return;

        HashMap<Integer, Integer> pwdMap = new HashMap<>(originalPasswordArray.length);

        for (int i =0; i < originalPasswordArray.length; ++i) {
            pwdMap.put(originalPasswordArray[i], i);
        }

        Arrays.fill(prefixes, -1);

        for (int i = 0; i < message.solution.length; ++i) {
            prefixes[pwdMap.get(message.solution[i])] = 1;
        }
        done = true;
        endTime = System.currentTimeMillis();
    }

    private static int fillStack(Stack<Integer> stack, int[] elements, int startIndex, int sumOnStack, int maxValue)
    {
        while (sumOnStack < maxValue && startIndex < elements.length)
        {
            stack.push(startIndex);
            sumOnStack += elements[startIndex];
            startIndex++;
        }

        return sumOnStack;
    }

    private static long compressStack(Stack<Integer> stack)
    {
        long result = 0;

        for (int e : stack)
            result = result | (1l << e);

        return result;
    }

    public static int[] handle(FindLinearCombinationMessage message) {

        Stack<Integer> selectedIndices = new Stack<>();

        int sumOnStack = message.initializeStack(selectedIndices);
        // If sumOnStack is greater than sum, this prefix does not have
        // to be further evaluated.
        // Todo: Shouldn't this take place in the master?
        if (sumOnStack > message.sum)
            return new int[0];

        sumOnStack = fillStack(selectedIndices, message.passwords, message.prefixLength, sumOnStack, message.sum);

        // Todo: We should probably consider a timeout
        while (sumOnStack != message.sum && selectedIndices.size() > 1)
        {
            int index = selectedIndices.pop();
            sumOnStack -= message.passwords[index];
            index = selectedIndices.pop();
            sumOnStack -= message.passwords[index];

            if (index < message.prefixLength)
                break;

            sumOnStack = fillStack(selectedIndices, message.passwords, index + 1, sumOnStack, message.sum);
        }

        if (sumOnStack == message.sum) {
            return selectedIndices.stream().mapToInt((i)->message.passwords[i]).toArray();
        }

        return new int[0];
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
