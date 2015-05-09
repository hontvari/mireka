package mireka.submission;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IpAddress specifies a single IP address or a subnet.
 */
public class IpAddress {
    private final String name;
    private final byte[] subnetBytes;
    private final int bits;
    private final int cCompleteBytes;
    private final boolean isPartialByteComparisonRequired;
    private final byte partialByteMask;

    /**
     * @param addressSpecification
     *            in CIDR notation, like "192.168.0.0/24"
     */
    public IpAddress(String addressSpecification) {
        name = addressSpecification;
        try {
            int iSlash = addressSpecification.lastIndexOf('/');
            if (iSlash == -1) {
                subnetBytes =
                        InetAddress.getByName(addressSpecification)
                                .getAddress();
                bits = subnetBytes.length * 8;
            } else {
                String addressPart = addressSpecification.substring(0, iSlash);
                subnetBytes = InetAddress.getByName(addressPart).getAddress();
                String bitsString = addressSpecification.substring(iSlash + 1);
                bits = Integer.valueOf(bitsString);
            }

            cCompleteBytes = bits / 8;
            int cPartialBits = bits % 8;
            isPartialByteComparisonRequired = cPartialBits != 0;
            partialByteMask = (byte) (0xFF00 >> cPartialBits);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException();
        }
    }

    boolean isSatisfiedBy(InetAddress inetAddress) {
        byte[] actualBytes = inetAddress.getAddress();
        int i = 0;
        while (i < cCompleteBytes) {
            if (subnetBytes[i] != actualBytes[i])
                return false;
            i++;
        }
        if (!isPartialByteComparisonRequired)
            return true;
        return (subnetBytes[i] & partialByteMask) == (actualBytes[i] & partialByteMask);
    }

    @Override
    public String toString() {
        return name;
    }
}
