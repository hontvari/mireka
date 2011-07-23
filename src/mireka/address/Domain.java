package mireka.address;

import java.util.Locale;

import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

/**
 * It represents an absolute domain name.
 */
public class Domain {
    private final String value;
    private final String valueInLowerCase;

    public Domain(String value) {
        this.value = value;
        this.valueInLowerCase = value.toLowerCase(Locale.US);
    }

    /**
     * Returns the raw domain text, as it was supplied in the SMTP transaction.
     */
    public String smtpText() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((valueInLowerCase == null) ? 0 : valueInLowerCase
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
        Domain other = (Domain) obj;
        if (valueInLowerCase == null) {
            if (other.valueInLowerCase != null)
                return false;
        } else if (!valueInLowerCase.equals(other.valueInLowerCase))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * converts this value to a dnsjava absolute domain name, assuming that the
     * name represented by this object is absolute. Domains must be absolute in
     * SMTP, except perhaps in a submission server.
     * 
     * @throws RuntimeException
     *             if the name in this object is syntactically invalid as a
     *             domain name
     */
    public Name toName() throws RuntimeException {
        String absoluteNameString = value.endsWith(".") ? value : value + ".";
        try {
            return Name.fromString(absoluteNameString);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }
}
