package mireka.filter.local.table;

import mireka.address.LocalPart;
import mireka.address.RemotePartContainingRecipient;

public class AnyDomainRecipient implements RecipientSpecification {
    private LocalPart localPart;

    @Override
    public boolean isSatisfiedBy(RemotePartContainingRecipient recipient) {
        return localPart.equals(recipient.getAddress().getLocalPart());
    }

    /**
     * @category GETSET
     */
    public LocalPart getLocalPart() {
        return localPart;
    }

    /**
     * @category GETSET
     */
    public void setLocalPart(LocalPart localPart) {
        this.localPart = localPart;
    }
}
