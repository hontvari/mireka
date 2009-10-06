package mireka.mailaddress;


public class DomainPart implements RemotePart {
    public final Domain domain;

    public DomainPart(String domainPart) {
        this.domain = new Domain(domainPart);
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
