package mireka.imap.parser;

/**
 * Indicates that instead of sending a new command the client closed the connection.
 * 
 * This exception is only used if no characters are received when reading a new command. If the EOF
 * comes unexpectedly, in the middle of the command, then another exception must be thrown. The
 * reason for this, is that some client at the end of the session occasionally ant intentionally
 * simply closes the connection instead of a proper logout handshake.
 */
public class NoMoreLinesException extends Exception {
    private static final long serialVersionUID = -2525573843963244741L;

    public NoMoreLinesException() {
        super("Client unexpectedly closed its output stream");
    }
}
