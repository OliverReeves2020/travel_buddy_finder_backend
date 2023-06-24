package json;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Random {
    private UUID[] data;
    //private OffsetDateTime completionTime;

    public UUID[] getData() { return data; }
    public void setData(UUID[] value) { this.data = value; }

    //public OffsetDateTime getCompletionTime() { return completionTime; }
    //public void setCompletionTime(OffsetDateTime value) { this.completionTime = value; }
}
