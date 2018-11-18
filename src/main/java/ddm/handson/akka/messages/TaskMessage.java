package ddm.handson.akka.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TaskMessage implements Serializable {
    private static final long serialVersionUID = 5157329686450190837L;
    public final String content;
}
