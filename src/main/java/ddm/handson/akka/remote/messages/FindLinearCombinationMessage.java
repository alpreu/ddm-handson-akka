package ddm.handson.akka.remote.messages;

import java.io.Serializable;
import java.util.Stack;

public class FindLinearCombinationMessage implements Serializable
{
    public final long prefix;
    public final int prefixLength;
    public final int sum;
    public final int[] passwords;

    public FindLinearCombinationMessage(long prefix, int prefixLength,  int sum, int[] ascSortedPasswords) {
        this.prefix = prefix;
        this.prefixLength = prefixLength;
        this.sum = sum;
        this.passwords = ascSortedPasswords;
    }

    /**
     * Initializes the stack. This means, it fills the stack with the items encoded in prefix.
     * @param stack Empty stack to fill
     * @return The sum of the items on the stack.
     */
    public int initializeStack(Stack<Integer> stack)
    {
        int sumOnStack = 0;

        for (int i = 0; i < prefixLength; ++i)
        {
            if (((prefix >> i) & 1) == 1) {
                stack.push(i);
                sumOnStack += passwords[i];
            }
        }

        return sumOnStack;
    }
}
