package mireka.dmarc;

public class RecoverableDmarcException extends Exception {
    private static final long serialVersionUID = -1649406468138868277L;

    public RecoverableDmarcException(String message) {
        super(message);
    }

    public RecoverableDmarcException(String message, Throwable cause) {
        super(message, cause);
    }
}
