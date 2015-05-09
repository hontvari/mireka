package mireka.smtp.address;

/**
 * RealReversePath is a non-null reverse path, supplied in the SMTP MAIL
 * command.
 */
public class RealReversePath implements ReversePath {
    private final Mailbox mailbox;

    public RealReversePath(Mailbox mailbox) {
        this.mailbox = mailbox;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String getSmtpText() {
        return mailbox.getSmtpText();
    }

    public Mailbox getMailbox() {
        return mailbox;
    }

    @Override
    public String toString() {
        return mailbox.toString();
    }
}
