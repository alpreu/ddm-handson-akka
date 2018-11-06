package ddm.handson.akka;
import akka.actor.ActorSystem;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args)
    {
        System.out.println( "Testing project setup..." );
        ActorSystem system = ActorSystem.create("ExampleActorSystem");
        System.out.println("Successfully created actor system: " + system.name()+ ".");
        system.shutdown();
    }
}
