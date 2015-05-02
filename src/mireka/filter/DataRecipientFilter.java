package mireka.filter;

import mireka.maildata.Maildata;
import mireka.smtp.RejectExceptionExt;

/**
 * a simpler alternative to {@link Filter}. It must be adapted to {@link Filter}
 * using {@link DataRecipientFilterAdapter}
 */
public interface DataRecipientFilter extends FilterBase {

    /**
     * This method and the {@link #data} method are called together, the similar
     * methods of the next filter will be called only after both methods have
     * been run.
     */
    void dataRecipient(Maildata data, RecipientContext recipientContext)
            throws RejectExceptionExt;
}
