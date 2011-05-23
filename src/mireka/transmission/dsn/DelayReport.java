package mireka.transmission.dsn;

/**
 * DelayReport contains all information necessary to produce a "delayed"
 * recipient section in a DSN message.
 */
public class DelayReport extends RecipientProblemReport {
    @Override
    public String actionCode() {
        return "delayed";
    }

    @Override
    public String toString() {
        return "DelayReport [recipient=" + recipient + ", status=" + status
                + "]";
    }

}
