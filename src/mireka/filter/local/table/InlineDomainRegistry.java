package mireka.filter.local.table;

import java.util.HashSet;
import java.util.Set;

import mireka.address.AddressLiteral;
import mireka.address.MailAddressFactory;
import mireka.address.RemotePart;

/**
 * InlineDomainRegistry is used to easily configure the list of domains and
 * address literals which are considered local by the mail server. In contrast
 * to its name this container also accepts {@link AddressLiteral}, but that is
 * rarely used if ever.
 */
public class InlineDomainRegistry implements RemotePartSpecification {
    private final Set<RemotePart> remoteParts = new HashSet<RemotePart>();

    @Override
    public boolean isSatisfiedBy(RemotePart remotePart) {
        return remoteParts.contains(remotePart);
    }

    public void addDomain(String remotePart) {
        remoteParts.add(new MailAddressFactory()
                .createRemotePartFromDisplayableText(remotePart));
    }

    public void setRemoteParts(String[] remoteParts) {
        this.remoteParts.clear();
        for (String remotePart : remoteParts) {
            addDomain(remotePart);
        }
    }

}
