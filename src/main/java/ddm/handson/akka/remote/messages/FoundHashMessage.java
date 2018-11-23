package ddm.handson.akka.remote.messages;

import java.io.Serializable;

public class FoundHashMessage implements Serializable
{
    public final String hash;

    public FoundHashMessage(String hash) {
        this.hash = hash;
    }
}
