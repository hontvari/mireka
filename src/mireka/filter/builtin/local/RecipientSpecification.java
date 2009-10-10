package mireka.filter.builtin.local;

import mireka.mailaddress.RemotePartContainingRecipient;

public interface RecipientSpecification {
    public boolean isSatisfiedBy(RemotePartContainingRecipient recipient);
}
