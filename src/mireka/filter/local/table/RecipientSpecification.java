package mireka.filter.local.table;

import mireka.address.Recipient;

/**
 * A RecipientSpecification defines a criteria and it is able to tell if a
 * recipient matches it.
 */
public interface RecipientSpecification {
    boolean isSatisfiedBy(Recipient recipient);
}
