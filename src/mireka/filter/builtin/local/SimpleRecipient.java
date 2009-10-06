package mireka.filter.builtin.local;

import mireka.mailaddress.Recipient;

public class SimpleRecipient implements RecipientSpecification {
    private Recipient recipient;

    @Override
    public boolean isSatisfiedBy(Recipient recipient2) {
        return recipient.equals(recipient2);
    }

    public SimpleRecipient(Recipient recipient) {
        this.recipient = recipient;
    }
    
}
