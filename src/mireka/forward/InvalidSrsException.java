package mireka.forward;

import mireka.smtp.EnhancedStatus;

/**
 * Thrown if a seemingly SRS compatible reverse path has illegal syntax,
 * expired, or has invalid digital signature.
 */
public class InvalidSrsException extends Exception {
    private static final long serialVersionUID = 4328204555021675703L;
    private final EnhancedStatus status;

    public InvalidSrsException(String message, EnhancedStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * @category GETSET
     */
    public EnhancedStatus getStatus() {
        return status;
    }

}
