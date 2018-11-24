package ddm.handson.akka.messages;

import java.io.Serializable;

public class FoundHashMessage implements Serializable
{
    private static final long serialVersionUID = -5513183352895539805L;
    public final String hash;

    @SuppressWarnings("unused")
    public FoundHashMessage() {
        this("");
    }

    public FoundHashMessage(String hash) {
        this.hash = hash;
    }
}