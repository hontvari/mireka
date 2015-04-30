package mireka.filter;

import java.io.IOException;

import mireka.address.ReversePath;
import mireka.maildata.MaildataFile;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

public abstract class AbstractDataRecipientFilter implements
        DataRecipientFilter {
    protected final MailTransaction mailTransaction;

    protected AbstractDataRecipientFilter(MailTransaction mailTransaction) {
        this.mailTransaction = mailTransaction;
    }

    @Override
    public void begin() {
        // do nothing
    }

    @Override
    public void from(ReversePath from) {
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
    public void data(MaildataFile data) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        // do nothing
    }

    @Override
    public void dataRecipient(MaildataFile data, RecipientContext recipientContext)
            throws RejectExceptionExt {
        // do nothing
    }

    @Override
    public void done() {
        // do nothing
    }
}
