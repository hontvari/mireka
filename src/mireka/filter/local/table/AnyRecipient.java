package mireka.filter.local.table;

import mireka.address.Recipient;

/**
 * AnyRecipient matches any recipient, for any domain, including the reserved
 * postmaster mailboxes.
 */
public class AnyRecipient implements RecipientSpecification {

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        return true;
    }
}
