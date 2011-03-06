package mireka.smtp;

import org.subethamail.smtp.RejectException;

/**
 * Indicates that the SMTP command just received from a client must be rejected.
 * This exception corresponds to the SubEthaSMTP {@link RejectException}, but it
 * uses SMTP enhanced mail system status codes and it is a checked exception.
 */
public class RejectExceptionExt extends Exception {
    private static final long serialVersionUID = 1530775593602824560L;

    private final EnhancedStatus reply;

    public RejectExceptionExt(EnhancedStatus reply) {
        this.reply = reply;
    }

    public EnhancedStatus getReply() {
        return reply;
    }

    /**
     * Converts this exception to a SubEthaSMTP {@link RejectException}.
     * 
     * @return the {@link RejectException} corresponding to this exception.
     */
    public RejectException toRejectException() {
        return new RejectException(reply.getSmtpReplyCode(),
                reply.getMessagePrefixedWithEnhancedStatusCode());
    }
}
