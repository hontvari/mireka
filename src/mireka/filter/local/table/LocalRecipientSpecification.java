package mireka.filter.local.table;

import mireka.address.Address;
import mireka.address.Recipient;
import mireka.address.RemotePartContainingRecipient;

/**
 * LocalRecipientSpecification matches a recipient if its remote part matches
 * the supplied {@link RemotePartSpecification} and its local part matches the
 * supplied {@link LocalPartSpecification}.
 */
public class LocalRecipientSpecification implements RecipientSpecification {
    private LocalPartSpecification localPartSpecification;
    private RemotePartSpecification remotePartSpecification;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        if (!(recipient instanceof RemotePartContainingRecipient))
            return false;
        if (!localPartSpecification.isSatisfiedBy(recipient.localPart()))
            return false;
        Address address =
                ((RemotePartContainingRecipient) recipient).getAddress();
        return remotePartSpecification.isSatisfiedBy(address.getRemotePart());
    }

    /**
     * @category GETSET
     */
    public LocalPartSpecification getLocalPartSpecification() {
        return localPartSpecification;
    }

    /**
     * @category GETSET
     */
    public void setLocalPartSpecification(
            LocalPartSpecification localPartSpecification) {
        this.localPartSpecification = localPartSpecification;
    }

    /**
     * @category GETSET
     */
    public RemotePartSpecification getRemotePartSpecification() {
        return remotePartSpecification;
    }

    /**
     * @category GETSET
     */
    public void setRemotePartSpecification(
            RemotePartSpecification remotePartSpecification) {
        this.remotePartSpecification = remotePartSpecification;
    }
}
