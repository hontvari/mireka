package mireka.imap.store;

import mireka.imap.CompletionException;
import mireka.imap.ResponseCode;

public class AlreadyExistsException extends CompletionException {
    private static final long serialVersionUID = -1125802569074914392L;

    public AlreadyExistsException(String message) {
        super(ResponseCode.ALREADYEXISTS, message);
    }
}
