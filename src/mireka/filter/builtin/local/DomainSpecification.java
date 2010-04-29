package mireka.filter.builtin.local;

import mireka.address.RemotePart;

public interface DomainSpecification {
    public boolean isSatisfiedBy(RemotePart remotePart);
}
