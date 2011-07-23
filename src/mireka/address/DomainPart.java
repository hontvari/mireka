package mireka.address;

/**
 * DomainPart is a remote part which is specified as a DNS domain, for example
 * example.com in the john@example.com address.
 */
public class DomainPart implements RemotePart {
    public final Domain domain;

    public DomainPart(Domain domain) {
        this.domain = domain;
    }

    public DomainPart(String domain) {
        this(new Domain(domain));
    }

    @Override
    public String smtpText() {
        return domain.smtpText();
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
        DomainPart other = (DomainPart) obj;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return domain.toString();
    }
}
