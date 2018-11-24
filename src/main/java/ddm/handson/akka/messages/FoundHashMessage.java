package ddm.handson.akka.messages;

import java.io.Serializable;

public class FoundHashMessage implements Serializable
{
    public final String hash;

    @SuppressWarnings("unused")
    public FoundHashMessage() {
        this("");
    }

    public FoundHashMessage(String hash) {
        this.hash = hash;
    }
}