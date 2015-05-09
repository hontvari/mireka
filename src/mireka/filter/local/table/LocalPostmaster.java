package mireka.filter.local.table;

import mireka.smtp.address.DomainPostmaster;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.RemotePart;

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
                ((DomainPostmaster) recipient).getMailbox().getRemotePart();
        return localDomains.isSatisfiedBy(remotePart);
    }

    /**
     * @x.category GETSET
     */
    public void setLocalDomains(RemotePartSpecification remotePartSpecification) {
        this.localDomains = remotePartSpecification;
    }

    /**
     * @x.category GETSET
     */
    public RemotePartSpecification getLocalDomains() {
        return localDomains;
    }

}
