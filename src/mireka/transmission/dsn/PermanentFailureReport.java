package mireka.transmission.dsn;

import java.util.Date;

import javax.annotation.Nullable;

import mireka.address.Recipient;
import mireka.transmission.EnhancedStatus;
import mireka.transmission.MailSystemStatus;
import mireka.transmission.immediate.RemoteMta;

public class PermanentFailureReport {
    public Recipient recipient;
    /**
     * @see <a href="http://tools.ietf.org/html/rfc3464#section-2.3.4">Status
     *      field</a>
     */
    public EnhancedStatus status;
    /**
     * @see <a
     *      href="http://tools.ietf.org/html/rfc3464#section-2.3.6">Diagnostic-Code
     *      field</a>
     */
    @Nullable
    public MailSystemStatus remoteMtaDiagnosticStatus;
    @Nullable
    public RemoteMta remoteMta;
    public Date failureDate;
    public String logId;

    @Override
    public String toString() {
        return "PermanentFailureReport [recipient=" + recipient + ", status="
                + status + "]";
    }
}