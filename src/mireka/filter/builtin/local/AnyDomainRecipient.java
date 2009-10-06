package mireka.filter.builtin.local;

import mireka.mailaddress.LocalPart;
import mireka.mailaddress.Recipient;
import mireka.mailaddress.RemotePartContainingRecipient;

public class AnyDomainRecipient implements RecipientSpecification {
    private LocalPart localPart;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        if (recipient.isGlobalPostmaster())
            return false;
        else
            return isSatisfiedBy((RemotePartContainingRecipient) recipient);
    }

    private boolean isSatisfiedBy(RemotePartContainingRecipient recipient) {
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
