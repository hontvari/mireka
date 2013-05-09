package mireka.smtp;

import java.util.Date;


/**
 * Signals an error occurred while attempting to transmit a mail to a remote
 * domain. Typically the remote system cannot be found in the DNS or it rejects
 * the mail.
 */
public class SendException extends Exception {
    private static final long serialVersionUID = 379604390803596371L;

    private final EnhancedStatus errorStatus;
    public final Date failureDate = new Date();
    /**
     * It must be set by the function which logs this exception by calling
     * {@link #initLogId}.
     */
    private String logId;

    public SendException(String message, EnhancedStatus status) {
        super(message);
        this.errorStatus = status;
    }

    public SendException(Throwable e, EnhancedStatus status) {
        super(e);
        this.errorStatus = status;
    }

    public SendException(String message, Throwable e, EnhancedStatus status) {
        super(message, e);
        this.errorStatus = status;
    }

    /**
     * @see <a href="http://tools.ietf.org/html/rfc3464#section-2.3.4">RFC 3464
     *      An Extensible Message Format for Delivery Status Notifications -
     *      Status field</a>
     */
    public EnhancedStatus errorStatus() {
        return errorStatus;
    }

    /**
     * It must be called if this exception gets logged. In theory Log-ID should
     * be set in the constructor if the cause was already logged. This never
     * happens, first of all because the logging framework doesn't know such
     * term as Log-ID.
     */
    public void initLogId(String logId) {
        this.logId = logId;
    }

    public String getLogId() {
        return logId;
    }
}
