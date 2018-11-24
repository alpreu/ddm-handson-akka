package ddm.handson.akka.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class WorkerNumberMessage implements Serializable {
    private static final long serialVersionUID = -1393040810390710323L;
    public final int numberOfWorkers;
}
