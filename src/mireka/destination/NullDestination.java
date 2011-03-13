package mireka.destination;

import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NullDestination drops the mail.
 */
public class NullDestination implements MailDestination {
    private final Logger logger = LoggerFactory
            .getLogger(NullDestination.class);

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        logger.debug("Mail is dropped for recipients, because their "
                + "destination is Null: " + mail.recipients.get(0)
                + (mail.recipients.size() >= 2 ? ", ..." : ""));
    }

    @Override
    public String toString() {
        return "NullDestination";
    }
}
