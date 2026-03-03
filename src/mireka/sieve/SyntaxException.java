package mireka.sieve;

public class SyntaxException extends Exception {
    private static final long serialVersionUID = -5192270584369353066L;

    public SyntaxException(String message) {
        super(message);
    }

    public SyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
