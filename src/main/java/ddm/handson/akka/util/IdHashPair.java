package ddm.handson.akka.util;

import java.io.Serializable;

public class IdHashPair implements Serializable {
    public final int id;
    public final String hash;

    @SuppressWarnings("unused")
    public IdHashPair()
    {
        this(0, "");
    }

    public IdHashPair(int id, String hash) {
        this.id = id;
        this.hash = hash;
    }
}