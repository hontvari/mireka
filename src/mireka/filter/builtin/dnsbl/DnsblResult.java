package mireka.filter.builtin.dnsbl;

import java.net.InetAddress;

/**
 * Represents the result of checking one or more DNSBL. If the identity is
 * checked on more then one DNSBL then this object describes the first positive
 * result, or the overall negative result.
 */
public class DnsblResult {
    public static final DnsblResult NOT_LISTED = new DnsblResult();

    public final boolean isListed;
    public final Dnsbl dnsbl;
    /**
     * Reason code returned by the DNSBL in the form of an IP address, usually
     * in the 127.0.0.x range.
     */
    public final InetAddress replyAddress;
    /**
     * Textual reason returned by the DNSBL service
     */
    private final String reason;

    /**
     * create a negative result
     */
    private DnsblResult() {
        this.isListed = false;
        this.dnsbl = null;
        this.replyAddress = null;
        this.reason = null;
    }

    /**
     * create a positive result
     */
    public DnsblResult(Dnsbl dnsbl, InetAddress replyAddress, String reason) {
        this.isListed = true;
        this.dnsbl = dnsbl;
        this.replyAddress = replyAddress;
        this.reason = reason;
    }

    public String getMessage() {
        if (!isListed)
            throw new IllegalStateException();
        return reason;
    }

    @Override
    public String toString() {
        if (!isListed)
            return "NOT LISTED";
        return "LISTED, dnsbl=" + dnsbl + ", replyAddress="
                + replyAddress.getHostAddress() + ", reason=" + reason;
    }
}