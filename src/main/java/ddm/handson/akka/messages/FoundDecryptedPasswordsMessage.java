package ddm.handson.akka.messages;

import ddm.handson.akka.util.IdPasswordPair;

import java.io.Serializable;

public class FoundDecryptedPasswordsMessage implements Serializable {
    private static final long serialVersionUID = -5545371305315488224L;
    public final IdPasswordPair[] passwords;

    @SuppressWarnings("unused")
    public FoundDecryptedPasswordsMessage() {
        this(null);
    }

    public FoundDecryptedPasswordsMessage(IdPasswordPair[] passwords) {
        this.passwords = passwords;
    }
}