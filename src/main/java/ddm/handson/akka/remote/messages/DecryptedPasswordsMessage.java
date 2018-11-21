package ddm.handson.akka.remote.messages;

import ddm.handson.akka.util.IdPasswordPair;

import java.io.Serializable;

public class DecryptedPasswordsMessage implements Serializable {
    public final IdPasswordPair[] passwords;

    public DecryptedPasswordsMessage(IdPasswordPair[] passwords) {
        this.passwords = passwords;
    }
}