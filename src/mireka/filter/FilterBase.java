package mireka.filter;

import java.io.IOException;

import mireka.mailaddress.Recipient;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * don't implement this interface directly, use the descendant interfaces and
 * the implementing abstract classes
 */
public interface FilterBase {

    void begin();

    void from(String from);

    /**
     * it is not called if a previous filter has already accepted the recipient
     */
    FilterReply verifyRecipient(Recipient recipient) throws RejectException;

    /**
     * it is only called if one of the filters accepted the recipient in
     * {@link #verifyRecipient}
     */
    void recipient(Recipient recipient) throws RejectException;

    void data(MailData data) throws RejectException, TooMuchDataException,
            IOException;

    /**
     * it is always called, even if some other filter failed or no mail was
     * delivered in this mail transaction
     */
    void done();

}