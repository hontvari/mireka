package mireka.filter.local.table;

import mireka.smtp.address.RemotePart;

/**
 * A DomainSpecification represents a constraint on a {@link RemotePart}, and it
 * can tell whether a mail address remote part matches it.
 */
public interface RemotePartSpecification {
    public boolean isSatisfiedBy(RemotePart remotePart);
}
