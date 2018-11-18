package ddm.handson.akka.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class WorkMessage implements Serializable {
    private static final long serialVersionUID = -7643194361832862395L;
    public final String content;
}