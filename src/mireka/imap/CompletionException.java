package mireka.imap;

/**
 * Results in a tagged NO response.
 */
public class CompletionException extends ImapException {
    private static final long serialVersionUID = 2408538402202189212L;

    public CompletionException(ResponseCode responseCode, String message) {
        super("NO", responseCode, message);
    }

    public CompletionException(String message) {
        this(null, message);
    }
}
