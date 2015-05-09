package mireka.filter.dnsbl;

import java.util.List;

import mireka.filter.MailTransaction;

/**
 * DnsblsChecker queries several Dnsbl instances and caches the result. If any
 * of the black lists returns a positive answer than the result is positive.
 */
public class DnsblsChecker {
    private final List<Dnsbl> blacklists;

    private final MailTransaction mailTransaction;

    protected DnsblsChecker(List<Dnsbl> blacklists,
            MailTransaction mailTransaction) {
        this.blacklists = blacklists;
        this.mailTransaction = mailTransaction;
    }

    private DnsblResult result;

    public DnsblResult getResult() {
        if (result == null)
            result = calculateResult();
        return result;
    }

    private DnsblResult calculateResult() {
        for (Dnsbl dnsbl : blacklists) {
            DnsblResult result =
                    dnsbl.check(mailTransaction.getRemoteInetAddress());
            if (result.isListed)
                return result;
        }
        return DnsblResult.NOT_LISTED;
    }
}