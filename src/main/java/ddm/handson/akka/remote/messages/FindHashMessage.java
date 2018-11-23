package ddm.handson.akka.remote.messages;

import java.io.Serializable;

public class FindHashMessage implements Serializable {
    public final int seed;
    public final int prefix;

    /**
     * Calculates a hash
     * @param seed Seed to use for randomizer
     * @param prefix If prefix == 1, a hash which starts with 11111 will be searched.
     *               If prefix == 0, a hash which starts with 11111 or 00000 will be searched.
     *               If prefix == -1, a hash which starts with 00000 will be searched.
     */
    public FindHashMessage(int seed, int prefix) {
        this.seed = seed;
        this.prefix = prefix;
    }
}