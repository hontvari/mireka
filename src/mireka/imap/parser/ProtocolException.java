package mireka.imap.parser;

import mireka.imap.ImapException;
import mireka.imap.ResponseCode;

/**
 * ProtocolException results in a BAD status response.
 */
public class ProtocolException extends ImapException {
    private static final long serialVersionUID = -5525422286623164488L;

    public ProtocolException(ResponseCode responseCode, String message) {
        super("BAD", responseCode, message);
    }

    public ProtocolException(String message) {
        this(null, message);
    }
}
