package mireka.filter;

import java.io.IOException;
import java.io.InputStream;

import mireka.address.ReversePath;
import mireka.maildata.Maildata;
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
    public void from(ReversePath from) throws RejectExceptionExt {
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
    public void dataStream(InputStream in) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        chain.dataStream(in);
    }

    @Override
    public void data(Maildata data) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        chain.data(data);
    }

    @Override
    public void done() {
        // do nothing
    }
}
