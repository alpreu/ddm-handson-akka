package ddm.handson.akka;

import java.io.Serializable;

public class TextMessage implements Serializable {
    private final String message;

    public TextMessage(String message)
    {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
