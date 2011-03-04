package mireka.filter.local.table;

import mireka.address.DomainPostmaster;
import mireka.address.Recipient;
import mireka.address.RemotePart;

/**
 * LocalPostmaster matches the special global postmaster and any domain
 * postmaster of the local domains as specified by a
 * {@link RemotePartSpecification}.
 */
public class LocalPostmaster implements RecipientSpecification {

    private RemotePartSpecification localDomains;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        if (recipient.isGlobalPostmaster())
            return true;
        if (!recipient.isDomainPostmaster())
            return false;
        RemotePart remotePart =
                ((DomainPostmaster) recipient).getAddress().getRemotePart();
        return localDomains.isSatisfiedBy(remotePart);
    }

    /**
     * @category GETSET
     */
    public void setLocalDomains(
            RemotePartSpecification remotePartSpecification) {
        this.localDomains = remotePartSpecification;
    }

    /**
     * @category GETSET
     */
    public RemotePartSpecification getLocalDomains() {
        return localDomains;
    }

}
