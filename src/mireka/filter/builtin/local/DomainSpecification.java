package mireka.filter.builtin.local;

import mireka.mailaddress.RemotePart;


public interface DomainSpecification {
    public boolean isSatisfiedBy(RemotePart remotePart);
}
