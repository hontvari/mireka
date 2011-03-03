package mireka.pop;

/**
 * Signals that a POP3 command sent by the client is syntactically invalid.
 */
public class CommandSyntaxException extends Pop3Exception {
    private static final long serialVersionUID = 8328847991438620703L;

    public CommandSyntaxException(String string) {
        super(null, string);
    }

}
