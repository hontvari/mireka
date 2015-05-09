package mireka.filter.local.table;

import mireka.smtp.address.GlobalPostmaster;
import mireka.smtp.address.Recipient;

/**
 * GlobalPostmasterSpecification matches only a {@link GlobalPostmaster}
 * recipient, i.e a recipient case insensitively named "Postmaster" (without any
 * remote part).
 */
public class GlobalPostmasterSpecification implements RecipientSpecification {

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        return recipient instanceof GlobalPostmaster;
    }

}
