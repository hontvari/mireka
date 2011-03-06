package mireka.filter;

import java.io.IOException;

import mireka.MailData;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

/**
 * Note: implementing classes must be thread safe, because they can be used by
 * multiple connections at the same time.
 */
public abstract class StatelessFilterType implements FilterType,
        DataRecipientFilter {

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new DataRecipientFilterAdapter(this, mailTransaction);
    }

    @Override
    public void begin() {
        // do nothing
    }

    @Override
    public void from(String from) {
        // do nothing
    }

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        return FilterReply.NEUTRAL;
    }

    @Override
    public void recipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        // do nothing
    }

    @Override
    public void data(MailData data) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        // do nothing
    }

    @Override
    public void dataRecipient(MailData data, RecipientContext recipientContext)
            throws RejectExceptionExt {
        // do nothing
    }

    @Override
    public void done() {
        // do nothing
    }
}
