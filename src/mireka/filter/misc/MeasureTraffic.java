package mireka.filter.misc;

import java.io.IOException;

import mireka.MailData;
import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterReply;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

/**
 * The MeasureTraffic filter collects statistics information about the incoming
 * traffic of a Mireka SMTP server port in an {@link IncomingSmtpSummary}
 * object.
 */
public class MeasureTraffic implements FilterType {
    private IncomingSmtpSummary incomingSmtpSummary;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction);
    }

    /**
     * @category GETSET
     */
    public void setIncomingSmtpSummary(IncomingSmtpSummary summary) {
        this.incomingSmtpSummary = summary;
    }

    private class FilterImpl extends AbstractFilter {

        public FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void begin() {
            incomingSmtpSummary.mailTransactions.mark();
            chain.begin();
        }

        @Override
        public void data(MailData data) throws RejectExceptionExt,
                TooMuchDataException, IOException {
            incomingSmtpSummary.dataCommands.mark();
            chain.data(data);
            incomingSmtpSummary.acceptedMessages.mark();
            incomingSmtpSummary.messageRecipients.mark(mailTransaction
                    .getAcceptedRecipientContexts().size());
        }

        @Override
        public FilterReply verifyRecipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            incomingSmtpSummary.rcptCommands.mark();
            return chain.verifyRecipient(recipientContext);
        }
    }
}
