package mireka.filter.local.table;

import mireka.address.RemotePart;

/**
 * A DomainSpecification represents a constraint, and it can tell if a mail
 * address remote part matches it.
 */
public interface DomainSpecification {
    public boolean isSatisfiedBy(RemotePart remotePart);
}
