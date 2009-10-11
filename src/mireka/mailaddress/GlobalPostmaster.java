package mireka.mailaddress;

/**
 * represents the special "Postmaster" recipient (without a domain). This is
 * always treated case-insensitively.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.1.1.3">RFC 5321
 *      4.1.1.3</a>
 */
public class GlobalPostmaster implements Recipient {
    /**
     * stored to preserve case
     */
    private final String text;

    public GlobalPostmaster(String postmaster) {
        this.text = postmaster;
    }

    @Override
    public boolean isDomainPostmaster() {
        return false;
    }

    @Override
    public boolean isGlobalPostmaster() {
        return true;
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
        return 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return text;
    }
}
