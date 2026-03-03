package mireka.filter.misc;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilter;
import mireka.maildata.io.MaildataReadException;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;

public class Mirror extends StatelessFilter {
    private final Logger logger = LoggerFactory.getLogger(Mirror.class);
    private Transmitter transmitter;

    @Override
    protected void data(MailTransaction transaction)
            throws RejectExceptionExt, MaildataReadException {
        try {
            Mail mail = createMail(transaction);
            transmitter.transmit(mail);
        } catch (LocalMailSystemException e) {
            logger.warn("Cannot accept mail because of a " + "transmission failure", e);
            throw new RejectException(e.errorStatus().getSmtpReplyCode(),
                    e.errorStatus().getMessagePrefixedWithEnhancedStatusCode());
        }
        super.data(transaction);
    }

    protected Mail createMail(MailTransaction transaction) {
        Mail mail = new Mail();
        mail.from = transaction.reversePath;
        for (RecipientContext recipientContext : transaction.recipientContexts) {
            mail.recipients.add(recipientContext.recipient);
        }
        mail.maildata = transaction.data;
        mail.arrivalDate = transaction.date.toInstant();
        mail.receivedFromMtaName = transaction.getMessageContext().getHelo();
        mail.receivedFromMtaAddress = transaction.getRemoteInetAddress();
        mail.scheduleDate = null;
        return mail;
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

    /**
     * @x.category GETSET
     */
    public Transmitter getTransmitter() {
        return transmitter;
    }
}
