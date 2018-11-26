package ddm.handson.akka.util;

import java.io.Serializable;

public class IdPasswordPair implements Serializable {
    private static final long serialVersionUID = -5501700020583494971L;
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