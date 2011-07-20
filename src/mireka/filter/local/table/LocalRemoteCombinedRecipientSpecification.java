package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;

/**
 * LocalRemoteCombinedRecipientSpecification combines a
 * {@link LocalPartSpecification} with a {@link RemotePart} value, both must
 * match to satisfy this specification.
 */
public class LocalRemoteCombinedRecipientSpecification implements
        RecipientSpecification {
    private LocalPartSpecification localPartSpecification;
    private RemotePart remotePart;

    public LocalRemoteCombinedRecipientSpecification(
            LocalPartSpecification localPartSpecification, RemotePart remotePart) {
        this.localPartSpecification = localPartSpecification;
        this.remotePart = remotePart;
    }

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        if (!localPartSpecification.isSatisfiedBy(recipient.localPart()))
            return false;
        if (!(recipient instanceof RemotePartContainingRecipient))
            return false;
        RemotePart recipientRemotePart =
                ((RemotePartContainingRecipient) recipient).getMailbox()
                        .getRemotePart();
        return remotePart.equals(recipientRemotePart);
    }

}
