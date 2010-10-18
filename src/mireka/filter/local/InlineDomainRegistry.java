package mireka.filter.local;

import java.util.HashSet;
import java.util.Set;

import mireka.address.AddressLiteral;
import mireka.address.MailAddressFactory;
import mireka.address.RemotePart;

/**
 * In contrast to its name this container also accepts {@link AddressLiteral},
 * but that is rarely used if ever.
 */
public class InlineDomainRegistry implements DomainSpecification {
    private Set<RemotePart> remoteParts = new HashSet<RemotePart>();

    @Override
    public boolean isSatisfiedBy(RemotePart remotePart) {
        return remoteParts.contains(remotePart);
    }

    public void addDomain(String remotePart) {
        remoteParts.add(new MailAddressFactory().createRemotePart(remotePart));
    }
}
