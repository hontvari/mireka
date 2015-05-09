package mireka.filter.local.table;

import mireka.smtp.address.Recipient;

/**
 * AnyPostmaster matches the special "Postmaster" (without remote part) and
 * "Postmaster@..." type of addresses, irrespective of their remote part.
 */
public class AnyPostmaster implements RecipientSpecification {

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        return recipient.isPostmaster();
    }
}
