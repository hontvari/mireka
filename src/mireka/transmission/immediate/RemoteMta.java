package mireka.transmission.immediate;

import javax.annotation.Nullable;

/**
 * Collects identification information about a remote MTA as they become
 * available, these informations are eventually used in delivery status
 * notifications
 */
public class RemoteMta {
    /**
     * Either a name in a DNS MX record or a literal address in square bracket.
     */
    public final String dnsName;
    /**
     * resolved address of {@link #dnsName} or null if {@link #dnsName} is not
     * yet resolved.
     */
    @Nullable
    public final String address;

    public RemoteMta(String dnsName) {
        this.dnsName = dnsName;
        this.address = null;
    }

    public RemoteMta(String dnsName, String address) {
        this.dnsName = dnsName;
        this.address = address;
    }

    @Override
    public String toString() {
        return dnsName + (address == null ? "" : " [" + address + "]");
    }

}