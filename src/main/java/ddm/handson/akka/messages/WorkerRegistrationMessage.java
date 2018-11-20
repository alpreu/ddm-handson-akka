package ddm.handson.akka.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class WorkerRegistrationMessage implements Serializable {
    private static final long serialVersionUID = -7789270944911671151L;
}