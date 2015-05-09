package mireka.smtp.address;

/**
 * This class represents a generic recipient, which is neither the special
 * global nor the special domain specific postmaster address.
 */
public class GenericRecipient implements RemotePartContainingRecipient {
    private final Mailbox mailbox;

    public GenericRecipient(Mailbox mailbox) {
        this.mailbox = mailbox;
    }

    public boolean isPostmaster() {
        return false;
    }

    public boolean isGlobalPostmaster() {
        return false;
    }

    public boolean isDomainPostmaster() {
        return false;
    }

    @Override
    public LocalPart localPart() {
        return mailbox.getLocalPart();
    }

    public String sourceRouteStripped() {
        return mailbox.toString();
    }

    public Mailbox getMailbox() {
        return mailbox;
    }

    @Override
    public String toString() {
        return mailbox.toString();
    }
}
