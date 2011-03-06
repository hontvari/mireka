package mireka.smtp;

import mireka.address.Recipient;

/**
 * Signals that the mailbox specified in the just received SMTP RCPT TO command
 * is unknown, so the command must be rejected.
 */
public class UnknownUserException extends RejectExceptionExt {
    private static final long serialVersionUID = 817326413250455976L;

    private final Recipient recipient;

    public UnknownUserException(Recipient recipient) {
        super(new EnhancedStatus(550, "5.1.1", "User unknown <" + recipient
                + ">"));
        this.recipient = recipient;
    }

    public Recipient getRecipient() {
        return recipient;
    }
}
