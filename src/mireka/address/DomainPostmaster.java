package mireka.address;

import java.util.Locale;

/**
 * represents the special "Postmaster@"domain recipient. This is always treated
 * case-insensitively.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.1.1.3">RFC 5321
 *      4.1.1.3</a>
 */
public class DomainPostmaster implements RemotePartContainingRecipient {
    private final String text;
    private final Domain domain;
    private final Address address;

    public DomainPostmaster(String domainPostmaster) {
        this.text = domainPostmaster;
        this.domain = parseDomain();
        this.address = new Address(domainPostmaster);
    }

    private Domain parseDomain() {
        String prefix = "postmaster@";
        String textLowerCase = text.toLowerCase(Locale.US);
        if (!textLowerCase.startsWith(prefix))
            throw new RuntimeException("Assertion failed");
        String domainText = text.substring(prefix.length());
        return new Domain(domainText);
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public boolean isDomainPostmaster() {
        return true;
    }

    @Override
    public boolean isGlobalPostmaster() {
        return false;
    }

    @Override
    public boolean isPostmaster() {
        return true;
    }

    @Override
    public String sourceRouteStripped() {
        return text;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
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
        DomainPostmaster other = (DomainPostmaster) obj;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return text;
    }

}
