package mireka.filter;

import java.io.IOException;

import mireka.mailaddress.Recipient;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public abstract class AbstractDataRecipientFilter implements DataRecipientFilter {
    protected final MailTransaction mailTransaction;

    protected AbstractDataRecipientFilter(MailTransaction mailTransaction) {
        this.mailTransaction = mailTransaction;
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
    public FilterReply verifyRecipient(Recipient recipient)
            throws RejectException {
        return FilterReply.NEUTRAL;
    }

    @Override
    public void recipient(Recipient recipient) throws RejectException {
        // do nothing
    }

    @Override
    public void data(MailData data) throws RejectException,
            TooMuchDataException, IOException {
        // do nothing
    }

    @Override
    public void dataRecipient(MailData data, Recipient recipient)
            throws RejectException {
        // do nothing
    }

    @Override
    public void done() {
        // do nothing
    }
}
