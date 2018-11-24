package ddm.handson.akka.util;

import java.io.Serializable;

public class IdPasswordPair implements Serializable {
    public final int id;
    public final int password;

    @SuppressWarnings("unused")
    public IdPasswordPair() {
        this(0, 0);
    }

    public IdPasswordPair(int id, int password) {
        this.id = id;
        this.password = password;
    }
}
