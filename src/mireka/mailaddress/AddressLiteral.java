package mireka.mailaddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AddressLiteral implements RemotePart {
    private final String value;
    private final InetAddress address;

    public AddressLiteral(String value) {
        this.value = value;
        this.address = parseInetAddress(value);
    }

    private InetAddress parseInetAddress(String value) {
        if (!value.startsWith("[") || !value.endsWith("]"))
            throw new IllegalArgumentException();
        String addressString = value.substring(1, value.length() - 1);
        try {
            return InetAddress.getByName(addressString);
        } catch (UnknownHostException e) {
            // impossible
            throw new RuntimeException("Assertion failed");
        }
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

    @Override
    public String toString() {
        return value;
    }
}
