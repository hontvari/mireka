package mireka.filter.local.table;

import mireka.smtp.address.DomainPostmaster;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.RemotePart;

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
     * @x.category GETSET
     */
    public RemotePart getRemotePart() {
        return remotePart;
    }

    /**
     * @x.category GETSET
     */
    public void setRemotePart(RemotePart remotePart) {
        this.remotePart = remotePart;
    }
}
