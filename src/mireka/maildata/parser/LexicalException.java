package mireka.maildata.parser;

/**
 * This exception is thrown when a scanner encounters an unexpected character.
 * The scanner catches this exception and returns a special ERROR token.
 */
public class LexicalException extends Exception {
    private static final long serialVersionUID = 5712274229135831546L;

    public String expected;
    public int position;

    public LexicalException(String expected, int position) {
        super("Expected: " + expected + " at position " + position);
        this.expected = expected;
        this.position = position;
    }
}
