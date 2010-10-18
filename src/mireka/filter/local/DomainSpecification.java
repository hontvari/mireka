package mireka.filter.local;

import mireka.address.RemotePart;

public interface DomainSpecification {
    public boolean isSatisfiedBy(RemotePart remotePart);
}
