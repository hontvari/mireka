package mireka.transmission.immediate.dns;

import mireka.transmission.EnhancedStatus;
import mireka.transmission.immediate.SendException;

public class MxLookupException extends SendException {
    private static final long serialVersionUID = 3089456869348639027L;

    public MxLookupException(Throwable cause, EnhancedStatus errorStatus) {
        super(cause, errorStatus);
    }

    public MxLookupException(String message, EnhancedStatus errorStatus) {
        super(message, errorStatus);
    }
}
