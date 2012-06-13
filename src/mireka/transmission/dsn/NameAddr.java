package mireka.transmission.dsn;

import org.apache.james.mime4j.dom.address.Mailbox;

/**
 * NameAddr class contains a display-name and an addr-spec.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5322#section-3.4">RFC 5322
 *      Internet Message Format - Address Specification</a>
 */
public class NameAddr {
    private String displayName;
    private String addressSpec;

    public NameAddr() {
        // setters will be used to set attributes
    }

    public NameAddr(String displayName, String addressSpec) {
        this.displayName = displayName;
        this.addressSpec = addressSpec;
    }

    public Mailbox toMime4jMailbox() {
        int iAt = addressSpec.indexOf('@');
        if (iAt == -1)
            throw new RuntimeException("Invalid address specification: "
                    + addressSpec);
        String localPart = addressSpec.substring(0, iAt);
        String domain = addressSpec.substring(iAt + 1);
        return new Mailbox(displayName, localPart, domain);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAddressSpec(String addressSpec) {
        this.addressSpec = addressSpec;
    }
}
