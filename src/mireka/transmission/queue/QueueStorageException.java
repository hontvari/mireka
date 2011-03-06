package mireka.transmission.queue;

import mireka.smtp.EnhancedStatus;
import mireka.transmission.LocalMailSystemException;

/**
 * Signals that a mail cannot be stored, moved or accepted because of a local
 * error.
 */
public class QueueStorageException extends LocalMailSystemException {
    private static final long serialVersionUID = 5384727056517662233L;

    /**
     * Constructs a new exception with the specified detail message.
     */
    public QueueStorageException(String message, EnhancedStatus errorStatus) {
        super(message, errorStatus);
    }

    public QueueStorageException(Throwable e, EnhancedStatus errorStatus) {
        super(e, errorStatus);
    }

    public QueueStorageException(EnhancedStatus errorStatus) {
        super(errorStatus);
    }
}
