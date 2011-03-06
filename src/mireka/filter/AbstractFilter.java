package mireka.filter;

import java.io.IOException;

import mireka.MailData;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

public abstract class AbstractFilter implements Filter {
    protected final MailTransaction mailTransaction;
    protected FilterChain chain;

    public AbstractFilter(MailTransaction mailTransaction) {
        this.mailTransaction = mailTransaction;
    }

    @Override
    public void setChain(FilterChain chain) {
        this.chain = chain;
    }

    @Override
    public void begin() {
        chain.begin();
    }

    @Override
    public void from(String from) throws RejectExceptionExt {
        chain.from(from);
    }

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        return chain.verifyRecipient(recipientContext);
    }

    @Override
    public void recipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        chain.recipient(recipientContext);
    }

    @Override
    public void data(MailData data) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        chain.data(data);
    }

    @Override
    public void done() {
        // do nothing
    }
}
