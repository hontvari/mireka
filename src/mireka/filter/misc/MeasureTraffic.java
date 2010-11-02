package mireka.filter.misc;

import java.io.IOException;

import mireka.MailData;
import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterReply;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class MeasureTraffic implements FilterType {
    private TrafficSummary trafficSummary;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction);
    }

    /**
     * @category GETSET
     */
    public TrafficSummary getTrafficSummary() {
        return trafficSummary;
    }

    /**
     * @category GETSET
     */
    public void setTrafficSummary(TrafficSummary summary) {
        this.trafficSummary = summary;
    }

    private class FilterImpl extends AbstractFilter {

        public FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void begin() {
            trafficSummary.mailTransactions.incrementAndGet();
            chain.begin();
        }

        @Override
        public void data(MailData data) throws RejectException,
                TooMuchDataException, IOException {
            trafficSummary.dataCommands.incrementAndGet();
            chain.data(data);
            trafficSummary.acceptedMessages.incrementAndGet();
            trafficSummary.messageRecipients.addAndGet(mailTransaction
                    .getAcceptedRecipientContexts().size());
        }

        @Override
        public FilterReply verifyRecipient(RecipientContext recipientContext)
                throws RejectException {
            trafficSummary.rcptCommands.incrementAndGet();
            return chain.verifyRecipient(recipientContext);
        }
    }
}
