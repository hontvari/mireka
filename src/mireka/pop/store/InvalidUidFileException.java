package mireka.pop.store;

/**
 * Indicates that the file storing the last allocated UID is invalid. This means
 * the maildrop is corrupt.
 */
public class InvalidUidFileException extends MaildropException {
    private static final long serialVersionUID = 1080341005147255875L;

    public InvalidUidFileException() {
        super();
    }

    public InvalidUidFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUidFileException(String message) {
        super(message);
    }

    public InvalidUidFileException(Throwable cause) {
        super(cause);
    }

}
