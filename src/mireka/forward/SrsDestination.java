package mireka.forward;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import mireka.destination.Session;
import mireka.destination.SessionDestination;
import mireka.filter.RecipientContext;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.address.NullReversePath;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.ReversePath;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SrsDestination assigned to a recipient indicates that the mail must be
 * forwarded to a remote address, which is specified in the Sender Rewriting
 * Scheme (SRS) compatible local part of the recipient address. These mails are
 * delivery status notifications related to a mail forwarded by this mail server
 * previously.
 */
public class SrsDestination implements SessionDestination {
    private final Logger logger = LoggerFactory.getLogger(SrsDestination.class);
    private Srs srs;
    /**
     * The transmitter which will be used to redistribute the incoming mail to
     * the encoded original source servers.
     */
    private Transmitter transmitter;

    @Override
    public Session createSession() {
        return new SessionImpl();
    }

    @Override
    public String toString() {
        return "SrsDestination";
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setSrs(Srs srs) {
        this.srs = srs;
    }

    /**
     * @x.category GETSET
     */
    public Srs getSrs() {
        return srs;
    }

    /**
     * @x.category GETSET
     */
    public Transmitter getTransmitter() {
        return transmitter;
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

    private class SessionImpl implements Session {
        private List<Recipient> sourceMailboxes = new ArrayList<Recipient>();

        @Override
        public void from(ReversePath from) throws RejectExceptionExt {
            // do nothing
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            try {
                Recipient originalSource =
                        srs.reverse(recipientContext.recipient);
                sourceMailboxes.add(originalSource);
            } catch (InvalidSrsException e) {
                logger.debug("SRS reverse expansion failed. " + e.getMessage());
                throw new RejectExceptionExt(e.getStatus());
            }
        }

        @Override
        public void data(Mail mail) throws RejectExceptionExt {
            try {
                if (!mail.from.isNull()) {
                    mail.from = new NullReversePath();
                    logger.debug("Valid SRS message is received with "
                            + "non-null reverse path. This contradicts the "
                            + "intended purpose of SRS. Mail will be "
                            + "forwarded with null reverse path anyway.");
                }
                mail.recipients.clear();
                mail.recipients.addAll(sourceMailboxes);
                transmitter.transmit(mail);
                logger.debug("SRS addresses was expanded and mail was "
                        + "submitted to transmitter: {}", mail);
            } catch (LocalMailSystemException e) {
                logger.error("Cannot transmit mail", e);
                throw new RejectExceptionExt(e.errorStatus());
            }
        }

        @Override
        public void done() {
            // nothing to do
        }

    }

}
