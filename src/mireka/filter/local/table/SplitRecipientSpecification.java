package mireka.filter.local.table;

import mireka.address.Mailbox;
import mireka.address.Recipient;
import mireka.address.RemotePartContainingRecipient;

/**
 * SplitRecipientSpecification matches a recipient if its remote part matches
 * the supplied {@link RemotePartSpecification} and its local part matches the
 * supplied {@link LocalPartSpecification}. This specification never matches the
 * global Postmaster address, because that has no remote part.
 */
public class SplitRecipientSpecification implements RecipientSpecification {
    private LocalPartSpecification localPartSpecification;
    private RemotePartSpecification remotePartSpecification;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        if (!(recipient instanceof RemotePartContainingRecipient))
            return false;
        if (!localPartSpecification.isSatisfiedBy(recipient.localPart()))
            return false;
        Mailbox mailbox =
                ((RemotePartContainingRecipient) recipient).getMailbox();
        return remotePartSpecification.isSatisfiedBy(mailbox.getRemotePart());
    }

    /**
     * @x.category GETSET
     */
    public LocalPartSpecification getLocalPartSpecification() {
        return localPartSpecification;
    }

    /**
     * @x.category GETSET
     */
    public void setLocalPartSpecification(
            LocalPartSpecification localPartSpecification) {
        this.localPartSpecification = localPartSpecification;
    }

    /**
     * @x.category GETSET
     */
    public RemotePartSpecification getRemotePartSpecification() {
        return remotePartSpecification;
    }

    /**
     * @x.category GETSET
     */
    public void setRemotePartSpecification(
            RemotePartSpecification remotePartSpecification) {
        this.remotePartSpecification = remotePartSpecification;
    }
}
