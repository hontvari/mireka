package mireka.pop;

/**
 * Indicates that the command received from the POP3 client cannot be run in the
 * current session state.
 */
public class IllegalSessionStateException extends Pop3Exception {
    private static final long serialVersionUID = -6495539321430267247L;

    public IllegalSessionStateException() {
        super(null, "Command is not allowed in the current state");
    }
}
