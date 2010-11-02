package mireka.address;

/**
 * GlobalPostmaster represents the special "Postmaster" recipient (without a
 * domain). This is always treated case-insensitively and quoted forms of the
 * name must not be used.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.1.1.3">RFC 5321
 *      4.1.1.3</a>
 */
public class GlobalPostmaster implements Recipient {
    private final LocalPart localPart;

    public GlobalPostmaster(String postmaster) {
        this.localPart = new LocalPart(postmaster);
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
    public LocalPart localPart() {
        return localPart;
    }

    @Override
    public String sourceRouteStripped() {
        return localPart.displayableName();
    }

    @Override
    public String toString() {
        return localPart.displayableName();
    }
}
