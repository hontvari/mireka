package mireka.filter.local;

import mireka.address.Recipient;
import mireka.address.RemotePartContainingRecipient;

public class SimpleRecipient implements RecipientSpecification {
    private Recipient recipient;

    @Override
    public boolean isSatisfiedBy(RemotePartContainingRecipient recipient2) {
        return recipient.equals(recipient2);
    }

    public SimpleRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

}
