package mireka.filter.local.table;

import mireka.smtp.address.Recipient;

/**
 * AnyDomainRecipient matches any recipient with the specified local part,
 * irrespective of the remote part. It can even match the special global
 * postmaster address which has no remote part at all.
 */
public class AnyDomainRecipient implements RecipientSpecification {
    private LocalPartSpecification localPart;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        return localPart.isSatisfiedBy(recipient.localPart());
    }

    /**
     * @x.category GETSET
     */
    public LocalPartSpecification getLocalPart() {
        return localPart;
    }

    /**
     * @x.category GETSET
     */
    public void setLocalPart(LocalPartSpecification localPart) {
        this.localPart = localPart;
    }
}
