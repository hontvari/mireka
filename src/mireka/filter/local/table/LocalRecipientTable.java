package mireka.filter.local.table;

import mireka.destination.Destination;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.RemotePart;
import mireka.smtp.address.RemotePartContainingRecipient;

/**
 * LocalRecipientTable contains recipient-destination mappings like
 * {@link RecipientTable}, but it never matches a recipient whose domain is not
 * included in the specified domain list.
 */
public class LocalRecipientTable extends RecipientTable {
    private RemotePartSpecification localDomains;

    @Override
    public Destination lookup(Recipient recipient) {
        if (recipient instanceof RemotePartContainingRecipient) {
            RemotePart remotePart =
                    ((RemotePartContainingRecipient) recipient).getMailbox()
                            .getRemotePart();
            if (!localDomains.isSatisfiedBy(remotePart))
                return null;
        }
        return super.lookup(recipient);
    }

    /**
     * @x.category GETSET
     */
    public RemotePartSpecification getLocalDomains() {
        return localDomains;
    }

    /**
     * @x.category GETSET
     */
    public void setLocalDomains(RemotePartSpecification localDomains) {
        this.localDomains = localDomains;
    }

}
