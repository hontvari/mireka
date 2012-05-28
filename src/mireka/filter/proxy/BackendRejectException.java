package mireka.filter.proxy;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.client.SMTPException;

/**
 * Indicates that the SMTP command from the client must be rejected because the
 * backend server in turn rejected the SMTP command sent from this server.
 */
public class BackendRejectException extends RejectException {
    private static final long serialVersionUID = 1213972117037757573L;

    public BackendRejectException(SMTPException cause, String comment) {
        super(cause.getResponse().getCode(), cause.getResponse().getMessage()
                + comment);
    }
}
