package ddm.handson.akka.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CompletionMessage implements Serializable {
    private static final long serialVersionUID = 6052013974178526720L;
    public enum status {OK, FAILED}
    public status result;
}
