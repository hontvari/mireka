package mireka.imap.store;

import mireka.imap.CompletionException;
import mireka.imap.ResponseCode;

/**
 * The server discovered that some relevant data (e.g., the mailbox) are corrupt.
 */
public class CorruptionException extends CompletionException {
    private static final long serialVersionUID = -7052540936745714031L;

    public CorruptionException(String message) {
        super(ResponseCode.CORRUPTION, message);
    }

    public CorruptionException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
