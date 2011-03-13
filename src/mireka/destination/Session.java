package mireka.destination;

import java.io.IOException;

import mireka.filter.FilterBase;
import mireka.filter.RecipientContext;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

/**
 * Session methods are called step by step as the SMTP mail transaction
 * progresses. The session object may directly affect the mail transaction e.g.
 * by rejecting a recipient. For each mail transaction a separate Session object
 * is created, so it can save states between the steps.
 * 
 * @see SessionDestination
 */
public interface Session {
    /**
     * Processes the reverse path. This function is called before the first
     * {@link #recipient(RecipientContext)} call.
     * 
     * @param from
     *            The reverse path.
     */
    void from(String from) throws RejectExceptionExt;

    /**
     * Processes an accepted recipient. It is only called if one of the filters
     * accepted the recipient in {@link FilterBase#verifyRecipient}.
     */
    void recipient(RecipientContext recipientContext) throws RejectExceptionExt;

    /**
     * Processes the mail after the mail data arrived. This function is called
     * after the SMTP DATA command has been received. It is not called if this
     * object rejected all recipients, or if other destination (assigned to
     * another destination) has already rejected the mail data.
     * 
     * @throws IOException
     *             if an error occurred while reading from the supplied mail
     *             data.
     */
    void data(Mail mail) throws RejectExceptionExt, IOException;

    /**
     * Closes this session. It is always called, even if some other function
     * failed or no mail was delivered in the session.
     */
    void done();

}
