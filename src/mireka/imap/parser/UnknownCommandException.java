package mireka.imap.parser;

public class UnknownCommandException extends CommandSyntaxException {
    private static final long serialVersionUID = 5370976688730456403L;

    public UnknownCommandException() {
        super("Command is not implemented");
    }
}
