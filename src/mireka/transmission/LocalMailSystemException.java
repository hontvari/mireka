package mireka.transmission;

/**
 * Signals a failure within the scope of this installation, in which the SMTP
 * protocol is not involved.
 * <p>
 * Failures can be transient or permanent. For example disk full is a transient
 * error. Transient status is indicated by the {@link #errorStatus()} value.
 * Processing which was stopped by a transient exception should be retried
 * later. There should be a limit on the count of attempts, except if it is not
 * only likely, but it is sure, that the condition is temporary.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc3463">RFC 3463 Enhanced Mail
 *      System Status Codes</a>
 */
public class LocalMailSystemException extends Exception {
    private static final long serialVersionUID = 3926566532324657918L;
    private final EnhancedStatus errorStatus;

    /**
     * Constructs a new exception where the message is coming from the
     * {@link EnhancedStatus}.
     */
    public LocalMailSystemException(EnhancedStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }

    /**
     * Constructs a new exception with the specified detail message.
     */
    public LocalMailSystemException(String message, EnhancedStatus errorStatus) {
        super(message);
        this.errorStatus = errorStatus;
    }

    /**
     * Constructs a new exception where the message is coming from the supplied
     * exception.
     */
    public LocalMailSystemException(Throwable cause, EnhancedStatus errorStatus) {
        super(cause);
        this.errorStatus = errorStatus;
    }

    /**
     * @see <a href="http://tools.ietf.org/html/rfc3464#section-2.3.4">Status
     *      field</a>
     */
    public EnhancedStatus errorStatus() {
        return errorStatus;
    }
}
