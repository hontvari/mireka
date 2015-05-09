package mireka.filter.local.table;

import mireka.smtp.address.Recipient;

/**
 * CaseInsensitiveAnyDomainRecipient is a convenience class for configuration,
 * it case insensitively matches any recipient with the specified local part,
 * irrespective of the remote part. It can even match the special global
 * postmaster address which has no remote part at all.
 */
public class CaseInsensitiveAnyDomainRecipient implements
        RecipientSpecification {
    private CaseInsensitiveLocalPartSpecification localPart;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        return localPart.isSatisfiedBy(recipient.localPart());
    }

    /**
     * @x.category GETSET
     */
    public void setLocalPart(String localPart) {
        this.localPart = new CaseInsensitiveLocalPartSpecification(localPart);
    }

}
