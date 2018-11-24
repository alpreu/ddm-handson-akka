package ddm.handson.akka.util;

import java.io.Serializable;

public class IdHashPair implements Serializable {
    private static final long serialVersionUID = 4621318228424340506L;
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