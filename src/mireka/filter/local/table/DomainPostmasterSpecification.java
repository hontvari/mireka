package mireka.filter.local.table;

import mireka.address.DomainPostmaster;
import mireka.address.Recipient;
import mireka.address.RemotePart;

/**
 * DomainPostmasterSpecification matches the domain postmaster of the supplied
 * remote part.
 */
public class DomainPostmasterSpecification implements RecipientSpecification {
    private RemotePart remotePart;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        if (!(recipient instanceof DomainPostmaster))
            return false;
        RemotePart recipientRemotePart =
                ((DomainPostmaster) recipient).getMailbox().getRemotePart();
        return remotePart.equals(recipientRemotePart);
    }

    /**
     * @category GETSET
     */
    public RemotePart getRemotePart() {
        return remotePart;
    }

    /**
     * @category GETSET
     */
    public void setRemotePart(RemotePart remotePart) {
        this.remotePart = remotePart;
    }
}
