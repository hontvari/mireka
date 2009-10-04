package mireka.filter.builtin.dnsbl;

import java.util.ArrayList;
import java.util.List;

import mireka.RejectExceptionExt;
import mireka.SmtpReply;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterReply;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.mailaddress.Recipient;

import org.subethamail.smtp.RejectException;

public class RefuseBlacklistedRecipient implements FilterType {
    private final List<Dnsbl> blacklists = new ArrayList<Dnsbl>();
    private SmtpReply smtpReply =
            new SmtpReply(530,
                    "Rejected: unauthenticated e-mail from {0} is restricted. "
                            + "Contact the postmaster for details.");

    public void addBlacklist(Dnsbl dnsbl) {
        if (dnsbl == null)
            throw new NullPointerException();
        blacklists.add(dnsbl);
    }

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        DnsblsChecker dnsblChecker =
                new DnsblsChecker(blacklists, mailTransaction);
        FilterImpl filterInstance =
                new FilterImpl(mailTransaction, dnsblChecker);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

    private class FilterImpl extends AbstractDataRecipientFilter {
        private final DnsblsChecker dnsblChecker;

        private FilterImpl(MailTransaction mailTransaction,
                DnsblsChecker dnsblChecker) {
            super(mailTransaction);
            this.dnsblChecker = dnsblChecker;
        }

        @Override
        public FilterReply verifyRecipient(Recipient recipient)
                throws RejectException {
            DnsblResult dnsblResult = dnsblChecker.getResult();
            if (dnsblResult.isListed) {
                SmtpReply smtpReply = calculateSmtpReply(dnsblResult);
                throw new RejectExceptionExt(smtpReply);
            }
            return FilterReply.NEUTRAL;
        }

        private SmtpReply calculateSmtpReply(DnsblResult dnsblResult) {
            SmtpReply reply =
                    dnsblResult.dnsbl.smtpReply.resolveDefaultsFrom(smtpReply);
            reply = reply.format(mailTransaction.getRemoteInetAddress());
            return reply;
        }
    }
}
