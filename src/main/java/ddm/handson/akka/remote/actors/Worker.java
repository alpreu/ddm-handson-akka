package ddm.handson.akka.remote.actors;


import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import ddm.handson.akka.IdPasswordPair;
import ddm.handson.akka.ProblemEntry;
import ddm.handson.akka.TextMessage;
import ddm.handson.akka.remote.messages.DecryptedPasswordsMessage;
import ddm.handson.akka.remote.messages.FindPasswordsMessage;
import scala.Int;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Worker extends AbstractLoggingActor {

    public static class FindLinearCombinationMessage implements Serializable
    {
        public final long prefix;
        public final byte prefixLength;
        public final int sum;
        public final int[] passwords;

        public FindLinearCombinationMessage(long prefix, byte prefixLength,  int sum, int[] ascSortedPasswords) {
            this.prefix = prefix;
            this.prefixLength = prefixLength;
            this.sum = sum;
            this.passwords = ascSortedPasswords;
        }

        public int initializeStack(Stack<Integer> stack)
        {
            int sumOnStack = 0;

            for (int i = 0; i < prefixLength && sumOnStack < sum; ++i)
            {
                if (((prefix >> i) & 1) == 1) {
                    stack.push(i);
                    sumOnStack += passwords[i];
                }
            }

            return sumOnStack;
        }
    }

    public static class LinearCombinationSolutionMessage implements Serializable
    {
        public final long solution;

        public LinearCombinationSolutionMessage(long solution) {
            this.solution = solution;
        }
    }



    @Override
    public Receive createReceive() {
        System.out.println("createReceive called.");
        return receiveBuilder()
                .match(TextMessage.class, this::handle)
                .match(FindPasswordsMessage.class, this::handle)
                .match(FindLinearCombinationMessage.class, this::handle)
                .matchAny(object -> System.out.println("Unknown message"))
                .build();
    }

    private static long compressStack(Stack<Integer> stack)
    {
        long result = 0;

        for (int e : stack)
            result = result | (1l << e);

        return result;
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

    private void handle(FindLinearCombinationMessage message) {

        Stack<Integer> selectedIndices = new Stack<>();
        int sumOnStack = message.initializeStack(selectedIndices);
        sumOnStack = fillStack(selectedIndices, message.passwords, message.prefixLength, sumOnStack, message.sum);

        while (sumOnStack != message.sum && selectedIndices.size() > 1)
        {
            int index = selectedIndices.pop();
            sumOnStack -= message.passwords[index];
            index = selectedIndices.pop();
            sumOnStack -= message.passwords[index];

            sumOnStack = fillStack(selectedIndices, message.passwords, index + 1, sumOnStack, message.sum);
        }

        if (sumOnStack == message.sum) {

            sender().tell(new LinearCombinationSolutionMessage(compressStack(selectedIndices)), self());
        }

        sender().tell(new LinearCombinationSolutionMessage(-1), self());
    }

    public static Props props()
    {
        return Props.create(Worker.class);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log().info("Worker {} shuttig down", self());
    }

    private String hash(int number) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(String.valueOf(number).getBytes("UTF-8"));

            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < hashedBytes.length; i++) {
                stringBuffer.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuffer.toString();
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void handle(TextMessage message) {
        System.out.println("Sender says: " + message.getMessage());
        this.sender().tell(new TextMessage("42"), this.self());
    }

    private void handle(FindPasswordsMessage message) {
        HashMap<String, Integer> hashs = new HashMap<>(message.upperBound - message.lowerBound + 1);
        for (int i = message.lowerBound; i <= message.upperBound; ++i) {
            hashs.put(hash(i), i);
        }

        List<IdPasswordPair> results = new ArrayList<>(42);

        for (ProblemEntry e : message.problemEntries) {
            int password = hashs.getOrDefault(e.getPassword(), Integer.MAX_VALUE);
            if (password < Integer.MAX_VALUE) {
                results.add(new IdPasswordPair(e.getId(), password));
            }
        }

        this.getSender().tell(new DecryptedPasswordsMessage(results.toArray(new IdPasswordPair[results.size()])), self());
    }
}
