package mireka.sieve;

public class CallbackException extends Exception {
    private static final long serialVersionUID = 1309212737865516414L;

    public final Reason reason;

    public CallbackException(Reason reason) {
        this.reason = reason;
    }

    public CallbackException(Reason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public CallbackException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public enum Reason {
        MAILBOX_NOT_EXISTS,
        /**
         * Subsystem is not available
         */
        UNAVAILABLE,
    }
}
