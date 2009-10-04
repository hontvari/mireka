package mireka.mailaddress;

import java.util.Locale;

/**
 * This implementation does case-insensitive comparisons using the US locale.
 * The standard SMTP only allows US-ASCII characters. Note that case sensitive
 * mailbox names are also allowed by the standard. An extension allows non-ASCII
 * characters and case insensitivity, but doesn't specify rules for that. E.g.
 * the final delivery mailbox address must also specify a Locale.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5336">RFC 5336 SMTP Extension for
 *      Internationalized Email Addresses</a>
 */
public class LocalPart {
    private final String value;
    private final String valueLowerCase;

    public LocalPart(String localPart) {
        this.value = localPart;
        this.valueLowerCase = localPart.toLowerCase(Locale.US);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((valueLowerCase == null) ? 0 : valueLowerCase
                                .hashCode());
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
        LocalPart other = (LocalPart) obj;
        if (valueLowerCase == null) {
            if (other.valueLowerCase != null)
                return false;
        } else if (!valueLowerCase.equals(other.valueLowerCase))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return value;
    }

}
