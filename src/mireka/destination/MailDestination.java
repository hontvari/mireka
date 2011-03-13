package mireka.destination;

import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

/**
 * MailDestination is a {@link ResponsibleDestination} which is able to deliver
 * the complete mail at the end of an SMTP mail transaction.
 */
public interface MailDestination extends ResponsibleDestination {
    /**
     * Processes the mail. This function is called after the SMTP DATA command
     * has been received.
     */
    void data(Mail mail) throws RejectExceptionExt;
}