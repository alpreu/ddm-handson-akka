package ddm.handson.akka;

import java.io.Serializable;

public class IdHashPair implements Serializable {
    public final int id;
    public final String hash;


    public IdHashPair(int id, String hash) {
        this.id = id;
        this.hash = hash;
    }
}
