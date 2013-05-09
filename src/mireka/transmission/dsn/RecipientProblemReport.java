package mireka.transmission.dsn;

import java.util.Date;

import javax.annotation.Nullable;

import mireka.address.Recipient;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.MailSystemStatus;
import mireka.smtp.client.MtaAddress;

/**
 * RecipientProblemReport contains information necessary to produce the
 * recipient specific part of a DSN message which indicates some kind of
 * problem.
 */
public abstract class RecipientProblemReport {

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
    public MtaAddress remoteMta;
    public Date failureDate;
    public String logId;

    /**
     * @see <a href="http://tools.ietf.org/html/rfc3464#section-2.3.3">Action
     *      field</a>
     */
    public abstract String actionCode();
}