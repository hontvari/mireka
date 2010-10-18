package mireka.filter.proxy;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.client.SMTPException;

public class BackendRejectException extends RejectException {
    private static final long serialVersionUID = 1213972117037757573L;

    public BackendRejectException(SMTPException cause, String comment) {
        super(cause.getResponse().getCode(), cause.getResponse().getMessage()
                + comment);
    }
}
