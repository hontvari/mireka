package mireka.imap;

/**
 * NonExistentException results in a NO result and the "NONEXISTENT" code
 */
public class NonExistentException extends CompletionException {
    private static final long serialVersionUID = 1417189257499994324L;

    public NonExistentException() {
        super(ResponseCode.NONEXISTENT, "No such mailbox");
    }

    public NonExistentException(ResponseCode code) {
        super(code, "No such mailbox");
    }
}
