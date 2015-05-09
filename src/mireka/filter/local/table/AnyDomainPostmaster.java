package mireka.filter.local.table;

import mireka.smtp.address.DomainPostmaster;
import mireka.smtp.address.Recipient;

/**
 * AnyDomainPostmaster matches the special Postmaster@... type of addresses,
 * irrespective of their remote part. Note that it does not match the global
 * postmaster address ("Postmaster", without any remote part).
 */
public class AnyDomainPostmaster implements RecipientSpecification {

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        return recipient instanceof DomainPostmaster;
    }
}
