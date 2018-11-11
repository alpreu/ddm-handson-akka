package ddm.handson.akka.remote.actors;


import akka.actor.AbstractActor;
import ddm.handson.akka.TextMessage;

public class Worker extends AbstractActor {



    @Override
    public Receive createReceive() {
        System.out.println("createReceive called.");
        return receiveBuilder()
                .match(TextMessage.class, this::handle)
                .matchAny(object -> System.out.println("Unknown message"))
                .build();
    }

    private void handle(TextMessage message) {
        System.out.println("Sender says: " + message.getMessage());
        this.sender().tell(new TextMessage("42"), this.self());
    }


}
