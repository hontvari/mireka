package mireka.util;

public class AssertionException extends RuntimeException {
    private static final long serialVersionUID = 6085414893552329630L;

    public AssertionException() {
        super("Assertion failed");
    }

    public AssertionException(Throwable cause) {
        super("Assertion failed", cause);
    }
}
