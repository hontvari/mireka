package mireka.filter.dnsbl;

import java.util.ArrayList;
import java.util.List;

import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterReply;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.SmtpReplyTemplate;

public class RefuseBlacklistedRecipient implements FilterType {
    private final List<Dnsbl> blacklists = new ArrayList<Dnsbl>();
    private SmtpReplyTemplate smtpReplyTemplate = new SmtpReplyTemplate(530,
            "5.7.1",
            "Rejected: unauthenticated e-mail from {0} is restricted. "
                    + "Contact the postmaster for details.");

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        DnsblsChecker dnsblChecker =
                new DnsblsChecker(blacklists, mailTransaction);
        FilterImpl filterInstance =
                new FilterImpl(mailTransaction, dnsblChecker);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
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

    private class FilterImpl extends AbstractDataRecipientFilter {
        private final DnsblsChecker dnsblChecker;

        private FilterImpl(MailTransaction mailTransaction,
                DnsblsChecker dnsblChecker) {
            super(mailTransaction);
            this.dnsblChecker = dnsblChecker;
        }

        @Override
        public FilterReply verifyRecipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            DnsblResult dnsblResult = dnsblChecker.getResult();
            if (dnsblResult.isListed) {
                EnhancedStatus smtpReply = calculateSmtpReply(dnsblResult);
                throw new RejectExceptionExt(smtpReply);
            }
            return FilterReply.NEUTRAL;
        }

        private EnhancedStatus calculateSmtpReply(DnsblResult dnsblResult) {
            SmtpReplyTemplate reply =
                    dnsblResult.dnsbl.smtpReplyTemplate
                            .resolveDefaultsFrom(smtpReplyTemplate);
            reply = reply.format(mailTransaction.getRemoteInetAddress());
            return reply.toEnhancedStatus();
        }
    }
}
