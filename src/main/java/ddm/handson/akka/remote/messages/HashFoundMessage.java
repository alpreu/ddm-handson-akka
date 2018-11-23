package ddm.handson.akka.remote.messages;

import java.io.Serializable;

public class HashFoundMessage implements Serializable
{
    public final String hash;

    public HashFoundMessage(String hash) {
        this.hash = hash;
    }
}
