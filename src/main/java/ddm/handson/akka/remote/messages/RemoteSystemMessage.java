package ddm.handson.akka.remote.messages;

import java.io.Serializable;
import akka.actor.Address;

public class RemoteSystemMessage implements Serializable {
    private final Address remoteAddress;

    public RemoteSystemMessage(Address remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
}
