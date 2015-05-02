package mireka.filter;

import java.io.IOException;
import java.io.InputStream;

import mireka.address.ReversePath;
import mireka.maildata.Maildata;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

/**
 * Adapts a {@link DataRecipientFilter} to the {@link Filter} interface
 */
public final class DataRecipientFilterAdapter implements Filter {
    private final DataRecipientFilter filter;
    private final MailTransaction mailTransaction;
    private FilterChain chain;

    public DataRecipientFilterAdapter(DataRecipientFilter filter,
            MailTransaction mailTransaction) {
        this.filter = filter;
        this.mailTransaction = mailTransaction;
    }

    @Override
    public void setChain(FilterChain chain) {
        this.chain = chain;
    }

    @Override
    public void begin() {
        filter.begin();
        chain.begin();
    }

    @Override
    public void from(ReversePath from) throws RejectExceptionExt {
        filter.from(from);
        chain.from(from);
    }

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        FilterReply result = filter.verifyRecipient(recipientContext);
        switch (result) {
        case ACCEPT:
            return FilterReply.ACCEPT;
        case NEUTRAL:
            return chain.verifyRecipient(recipientContext);
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void recipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        filter.recipient(recipientContext);
        chain.recipient(recipientContext);
    }

    @Override
    public void dataStream(InputStream in) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        filter.dataStream(in);
        chain.dataStream(in);
    }

    @Override
    public void data(Maildata data) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        filter.data(data);

        for (RecipientContext recipientContext : mailTransaction
                .getAcceptedRecipientContexts()) {
            filter.dataRecipient(data, recipientContext);
        }

        chain.data(data);
    }

    @Override
    public void done() {
        filter.done();
    }
}
