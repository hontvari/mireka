package mireka.filter.dnsbl;

import java.util.ArrayList;
import java.util.List;

import mireka.filter.Filter;
import mireka.filter.FilterSession;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.SmtpReplyTemplate;

/**
 * This filter rejects any recipient if the client SMTP server appears in any of
 * the configured DNS-based blackhole lists.
 */
public class RefuseBlacklistedRecipient implements Filter {
    private final List<Dnsbl> blacklists = new ArrayList<Dnsbl>();
    private SmtpReplyTemplate smtpReplyTemplate = new SmtpReplyTemplate(530,
            "5.7.1",
            "Rejected: unauthenticated e-mail from {0} is restricted. "
                    + "Contact the postmaster for details.");

    @Override
    public FilterSession createSession() {
        return new FilterImpl();
    }

    public void addBlacklist(Dnsbl dnsbl) {
        if (dnsbl == null)
            throw new NullPointerException();
        blacklists.add(dnsbl);
    }

    public void setBlacklists(List<Dnsbl> lists) {
        this.blacklists.clear();
        this.blacklists.addAll(lists);
    }

    private class FilterImpl extends FilterSession {
        private DnsblsChecker dnsblChecker;

        @Override
        public void begin() {
            dnsblChecker = new DnsblsChecker(blacklists, transaction);
            super.begin();
        }

        @Override
        public RecipientVerificationResult verifyRecipient(
                RecipientContext recipientContext) throws RejectExceptionExt {
            DnsblResult dnsblResult = dnsblChecker.getResult();
            if (dnsblResult.isListed) {
                EnhancedStatus smtpReply = calculateSmtpReply(dnsblResult);
                throw new RejectExceptionExt(smtpReply);
            }
            return RecipientVerificationResult.NEUTRAL;
        }

        private EnhancedStatus calculateSmtpReply(DnsblResult dnsblResult) {
            SmtpReplyTemplate reply =
                    dnsblResult.dnsbl.smtpReplyTemplate
                            .resolveDefaultsFrom(smtpReplyTemplate);
            reply = reply.format(transaction.getRemoteInetAddress());
            return reply.toEnhancedStatus();
        }
    }
}
