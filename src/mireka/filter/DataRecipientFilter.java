package mireka.filter;


import mireka.mailaddress.Recipient;

import org.subethamail.smtp.RejectException;

/**
 * a simpler alternative to {@link Filter}. It must be adapted to {@link Filter}
 * using {@link DataRecipientFilter}
 */
public interface DataRecipientFilter extends FilterBase {

    /**
     * This method and the {@link #data} method are called together,
     * the similar methods of the next filter will be called only after both
     * methods have been run.
     */
    void dataRecipient(MailData data, Recipient recipient)
            throws RejectException;
}
