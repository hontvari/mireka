package mireka.filter.local.table;

import mireka.smtp.address.LocalPart;

/**
 * LocalPartSpecification is able to decide if the local part of a recipient
 * address matches it. Local parts of local mailbox addresses are usually case
 * insensitive. In contrast to this, generic recipient addresses (forwarded to a
 * different domain) must be treated as if it were case sensitive.
 */
public interface LocalPartSpecification {
    boolean isSatisfiedBy(LocalPart localPart);
}
