package mireka.transmission.dsn;

/**
 * DelayReport contains all information necessary to produce a "failed"
 * recipient section in a DSN message.
 */
public class PermanentFailureReport extends RecipientProblemReport {
    @Override
    public String actionCode() {
        return "failed";
    }

    @Override
    public String toString() {
        return "PermanentFailureReport [recipient=" + recipient + ", status="
                + status + "]";
    }

}