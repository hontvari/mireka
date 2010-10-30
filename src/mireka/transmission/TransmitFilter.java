package mireka.transmission;

import java.io.IOException;
import java.util.Date;

import mireka.MailData;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.transmission.queuing.QueuingTransmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * This filter class asynchronously transmits the incoming mail using the
 * configured transmitter.
 */
public class TransmitFilter implements FilterType {
    private Transmitter transmitter;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        FilterImpl filterInstance = new FilterImpl(mailTransaction);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

    public void setTransmitter(QueuingTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    public Transmitter getTransmitter() {
        return transmitter;
    }

    private class FilterImpl extends AbstractDataRecipientFilter {
        private final Logger logger = LoggerFactory.getLogger(FilterImpl.class);

        private Mail mail = new Mail();

        protected FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void from(String from) {
            mail.from = from;
            mail.receivedFromMtaAddress =
                    mailTransaction.getRemoteInetAddress();
            mail.receivedFromMtaName =
                    mailTransaction.getMessageContext().getHelo();
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectException {
            if (!(recipientContext.getDestination() instanceof Transmit))
                return;
            recipientContext.isResponsibilityTransferred = true;
            mail.recipients.add(recipientContext.recipient);
        }

        @Override
        public void data(MailData data) throws TooMuchDataException,
                RejectException, IOException {
            if (mail.recipients.isEmpty())
                return;
            mail.mailData = data;
            mail.arrivalDate = new Date();
            mail.scheduleDate = mail.arrivalDate;
            try {
                transmitter.transmit(mail);
            } catch (LocalMailSystemException e) {
                logger.warn("Cannot accept mail because of a "
                        + "transmission failure", e);
                throw new RejectException(e.errorStatus().getSmtpReplyCode(), e
                        .errorStatus()
                        .getMessagePrefixedWithEnhancedStatusCode());
            }
        }
    }
}
