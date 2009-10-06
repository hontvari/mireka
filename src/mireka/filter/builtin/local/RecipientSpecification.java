package mireka.filter.builtin.local;

import mireka.mailaddress.Recipient;

public interface RecipientSpecification {
    public boolean isSatisfiedBy(Recipient recipient);
}
