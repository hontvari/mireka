package mireka.destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

public class CombinedMailDestination implements MailDestination {
    private final Logger logger = LoggerFactory.getLogger(CombinedMailDestination.class);
    private MailDestination d1;
    private MailDestination d2;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        RejectExceptionExt exception = null;
        try {
            d1.data(mail);
        } catch (RejectExceptionExt e) {
            logger.error("Exception in destination {} {}", d1, mail);
            exception = e;
        }
        try {
            d2.data(mail);
        } catch (RejectExceptionExt e) {
            logger.error("Exception in destination {} {}", d2, mail);
            exception = e;
        }
        if (exception != null)
            throw exception;
    }

    public void setD1(MailDestination d1) {
        this.d1 = d1;
    }

    public void setD2(MailDestination d2) {
        this.d2 = d2;
    }

}
