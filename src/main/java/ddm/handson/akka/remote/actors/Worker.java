package ddm.handson.akka.remote.actors;


import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import ddm.handson.akka.IdHashPair;
import ddm.handson.akka.IdPasswordPair;
import ddm.handson.akka.TextMessage;
import ddm.handson.akka.remote.messages.DecryptedPasswordsMessage;
import ddm.handson.akka.remote.messages.FindPasswordsMessage;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Worker extends AbstractLoggingActor {
//region messages
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

    public static class FindLCSMessage implements Serializable {
        public final int indexString1;
        public final int indexString2;
        public final String string1;
        public final String string2;

        public FindLCSMessage(int indexString1, int indexString2, String string1, String string2) {
            this.indexString1 = indexString1;
            this.indexString2 = indexString2;
            this.string1 = string1;
            this.string2 = string2;
        }
    }

    public static class FindHashesMessage implements Serializable
    {
        public final int seed;
        public final int id;
        public final int partnerId;
        public final boolean ones;

        public FindHashesMessage(int seed, int id, int partnerId, boolean ones) {
            this.seed = seed;
            this.id = id;
            this.partnerId = partnerId;
            this.ones = ones;
        }
    }
//endregion
    @Override
    public Receive createReceive() {
        System.out.println("createReceive called.");
        return receiveBuilder()
                .match(TextMessage.class, this::handle)
                .match(FindPasswordsMessage.class, this::handle)
                .match(FindLinearCombinationMessage.class, this::handle)
                .match(FindLCSMessage.class, this::handle)
                .match(FindHashesMessage.class, this::handle)
                .matchAny(object -> System.out.println("Unknown message"))
                .build();
    }

    private void handle(FindHashesMessage message) {
        Random rnd = new Random(message.seed);

        final String prefix = message.ones ? "11111" : "00000";
        String hash;
        int nonce;

        do {
            nonce = rnd.nextInt();
            hash = hash(nonce + message.partnerId);
        } while (!hash.startsWith(prefix));

        sender().tell(new Master.HashFoundMessage(message.id, hash), self());
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

        // TODO: Hier soll die Prefixlength das Abbruchkriterium sein.
        while (sumOnStack != message.sum && selectedIndices.size() > 1) // TODO: Hier wäre ein break möglich oder wir lösen das Problem mit einem TimeOut // Verwende Prefixlänge
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

    private final StringBuffer stringBuffer;
    public Worker()
    {
        stringBuffer = new StringBuffer(512);
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

            stringBuffer.setLength(0);
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
        HashMap<String, Integer> hashes = new HashMap<>(message.upperBound - message.lowerBound + 1);
        for (int i = message.lowerBound; i <= message.upperBound; ++i) {
            hashes.put(hash(i), i);
        }

        List<IdPasswordPair> results = new ArrayList<>(message.hashes.length);

        for (IdHashPair pair : message.hashes) {
            int password = hashes.getOrDefault(pair.hash, Integer.MAX_VALUE);
            if (password < Integer.MAX_VALUE) {
                results.add(new IdPasswordPair(pair.id, password));
            }
        }

        this.getSender().tell(new DecryptedPasswordsMessage(results.toArray(new IdPasswordPair[results.size()])), self());
    }

    // TODO: Longest common substring verwenden.
    private void handle(FindLCSMessage message) {

        int[][] matrix = new int[message.string1.length() + 1][message.string2.length() + 1];

        for (int i = 1; i <= message.string1.length(); ++i) {
            for (int j = 1; j <= message.string2.length(); ++j) {
                if (message.string1.charAt(i - 1) == message.string2.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                }
                else {
                    matrix[i][j] = Math.max(matrix[i - 1][j], matrix[i][j - 1]);
                }
            }
        }

        sender().tell(new Master.LCSMessage(message.indexString1, message.indexString2,
                matrix[message.string1.length()][message.string2.length()]), self());
    }
}
