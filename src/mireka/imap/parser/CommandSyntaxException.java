package mireka.imap.parser;

/**
 * Signals that a IMAP command sent by the client is syntactically invalid. This results in an 
 * IMAP BAD response.
 */
public class CommandSyntaxException extends ProtocolException {
    private static final long serialVersionUID = -6651006972122045540L;

    public CommandSyntaxException(String message) {
        super(null, message);
    }
}
