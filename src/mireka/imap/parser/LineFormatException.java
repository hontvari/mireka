package mireka.imap.parser;

import java.io.IOException;

/**
 * Signals an incomplete CRLF line ending, e.g. only a CR, not followed by LF.
 */
public class LineFormatException extends IOException {
    private static final long serialVersionUID = 996289996859983356L;

    public LineFormatException(String message) {
        super(message);
    }
}