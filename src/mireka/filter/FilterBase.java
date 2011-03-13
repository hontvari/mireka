package mireka.filter;

import java.io.IOException;

import mireka.MailData;
import mireka.destination.UnknownRecipientDestination;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * A filter processes mails, its functions are called in the different phases of
 * the SMTP mail transaction.
 * <p>
 * Note: Don't implement this interface directly, use the descendant interfaces
 * and the implementing abstract classes
 */
public interface FilterBase {

    void begin();

    void from(String from) throws RejectExceptionExt;

    /**
     * Decides if a recipient should be accepted. The decision can be a final
     * positive, a final negative, or a neutral answer. This function is not
     * called if a previous filter has already accepted or rejected the
     * recipient. In case of a neutral answer, other filters will decide. If all
     * filters return the neutral answer, then the recipient will be accepted if
     * a destination is assigned to it and the assigned destination is not an
     * {@link UnknownRecipientDestination}; otherwise it will be rejected as an
     * unknown user.
     * 
     * @throws RejectException
     *             if the recipient is not valid and it must be rejected
     */
    FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt;

    /**
     * Processes an accepted recipient. It is only called if one of the filters
     * accepted the recipient in {@link #verifyRecipient}.
     */
    void recipient(RecipientContext recipientContext) throws RejectExceptionExt;

    void data(MailData data) throws RejectExceptionExt, TooMuchDataException,
            IOException;

    /**
     * it is always called, even if some other filter failed or no mail was
     * delivered in this mail transaction
     */
    void done();

}