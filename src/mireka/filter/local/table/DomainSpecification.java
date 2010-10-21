package mireka.filter.local.table;

import mireka.address.RemotePart;

public interface DomainSpecification {
    public boolean isSatisfiedBy(RemotePart remotePart);
}
