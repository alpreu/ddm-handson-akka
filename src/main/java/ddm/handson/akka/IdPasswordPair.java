package ddm.handson.akka;

import java.io.Serializable;

public class IdPasswordPair implements Serializable {
    public final int id;
    public final int Password;

    public IdPasswordPair(int id, int password) {
        this.id = id;
        Password = password;
    }
}
