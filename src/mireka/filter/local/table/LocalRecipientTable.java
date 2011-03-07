package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;
import mireka.filter.Destination;

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
                    ((RemotePartContainingRecipient) recipient).getAddress()
                            .getRemotePart();
            if (!localDomains.isSatisfiedBy(remotePart))
                return UnknownRecipientDestination.INSTANCE;
        }
        return super.lookup(recipient);
    }

    /**
     * @category GETSET
     */
    public RemotePartSpecification getLocalDomains() {
        return localDomains;
    }

    /**
     * @category GETSET
     */
    public void setLocalDomains(RemotePartSpecification localDomains) {
        this.localDomains = localDomains;
    }

}
