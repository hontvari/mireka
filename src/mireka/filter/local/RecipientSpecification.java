package mireka.filter.local;

import mireka.address.RemotePartContainingRecipient;

public interface RecipientSpecification {
    public boolean isSatisfiedBy(RemotePartContainingRecipient recipient);
}
