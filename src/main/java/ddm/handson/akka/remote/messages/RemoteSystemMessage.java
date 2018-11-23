package ddm.handson.akka.remote.messages;

import akka.actor.Address;

import java.io.Serializable;

/**
 * Message received when a remote system connects to the master
 */
public class RemoteSystemMessage implements Serializable {
    public final Address remoteAddress;
    public final int numberOfWorkers;

    public RemoteSystemMessage(Address remoteAddress, int numberOfWorkers) {
        this.remoteAddress = remoteAddress;
        this.numberOfWorkers = numberOfWorkers;
    }
}
