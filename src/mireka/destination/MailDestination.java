package mireka.destination;

import mireka.maildata.Maildata;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

/**
 * A MailDestination is a {@link ResponsibleDestination} which is only interested 
 * in the complete mail at the end of the mail transaction, not in the steps of 
 * the mail transaction. 
 */
public interface MailDestination extends ResponsibleDestination {
    /**
     * Processes the mail. This function is called after the SMTP DATA command has been received. It
     * is only called if there is at least one accepted recipient.
     * 
     * The same mail instance may be passed for other destinations, so implementations must not
     * modify it. In addition to that, the {@link Maildata} within the mail object may be passed for
     * destinations assigned to other recipients of the original envelope. If modification is
     * required than an implementation must make a copy first.
     */
    void data(Mail mail) throws RejectExceptionExt;
}