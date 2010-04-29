package mireka;

import mireka.address.Recipient;

public class UnknownUserException extends RejectExceptionExt {
    private static final long serialVersionUID = 817326413250455976L;

    private final Recipient recipient;

    public UnknownUserException(Recipient recipient) {
        super(new SmtpReply(550, "User unknown <" + recipient + ">"));
        this.recipient = recipient;
    }

    public Recipient getRecipient() {
        return recipient;
    }
}
