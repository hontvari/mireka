package mireka.filter.misc;

import mireka.filter.Filter;
import mireka.filter.FilterSession;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.smtp.RejectExceptionExt;

/**
 * The MeasureTraffic filter collects statistics information about the incoming
 * traffic of a Mireka SMTP server port in an {@link IncomingSmtpSummary}
 * object.
 */
public class MeasureTraffic implements Filter {
    private IncomingSmtpSummary incomingSmtpSummary;

    @Override
    public FilterSession createSession() {
        return new FilterImpl();
    }

    /**
     * @x.category GETSET
     */
    public void setIncomingSmtpSummary(IncomingSmtpSummary summary) {
        this.incomingSmtpSummary = summary;
    }

    private class FilterImpl extends FilterSession {

        @Override
        public void begin() {
            incomingSmtpSummary.mailTransactions.mark();
            super.begin();
        }

        @Override
        public void data() throws RejectExceptionExt {
            incomingSmtpSummary.dataCommands.mark();
            super.data();
            incomingSmtpSummary.acceptedMessages.mark();
            incomingSmtpSummary.messageRecipients
                    .mark(transaction.recipientContexts.size());
        }

        @Override
        public RecipientVerificationResult verifyRecipient(
                RecipientContext recipientContext) throws RejectExceptionExt {
            incomingSmtpSummary.rcptCommands.mark();
            return super.verifyRecipient(recipientContext);
        }
    }
}
