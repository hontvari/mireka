package mireka.address;

import java.net.InetAddress;

/**
 * AddressLiteral is a remote part which is specified in IP address format, for
 * example [192.0.2.0] in the john@[192.0.2.0] address.
 */
public class AddressLiteral implements RemotePart {
    private final String smtpText;
    private final InetAddress address;

    public AddressLiteral(String smtpText, InetAddress inetAddress) {
        this.smtpText = smtpText;
        this.address = inetAddress;
    }

    public InetAddress inetAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AddressLiteral other = (AddressLiteral) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        return true;
    }

    /**
     * returns the address literal, including brackets, e.g. [192.0.2.0]
     */
    @Override
    public String toString() {
        return smtpText;
    }
}
