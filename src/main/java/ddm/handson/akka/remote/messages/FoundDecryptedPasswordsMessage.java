package ddm.handson.akka.remote.messages;

import ddm.handson.akka.util.IdPasswordPair;

import java.io.Serializable;

public class FoundDecryptedPasswordsMessage implements Serializable {
    public final IdPasswordPair[] passwords;

    @SuppressWarnings("unused")
    public FoundDecryptedPasswordsMessage() {
        this(null);
    }

    public FoundDecryptedPasswordsMessage(IdPasswordPair[] passwords) {
        this.passwords = passwords;
    }
}