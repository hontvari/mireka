package mireka.imap;

public class UnavailableException extends CompletionException {
    private static final long serialVersionUID = -2249710724168511095L;
    
    public UnavailableException() {
        super(ResponseCode.UNAVAILABLE, "Subsystem is down");
    }
    
    public UnavailableException(Throwable cause) {
        this();
        this.initCause(cause);
    }
}
