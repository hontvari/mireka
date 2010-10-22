package mireka.filter;

import java.io.IOException;

import mireka.MailData;

import org.subethamail.smtp.RejectException;
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
    public void from(String from) throws RejectException {
        filter.from(from);
        chain.from(from);
    }

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
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
    public void recipient(RecipientContext recipientContext) throws RejectException {
        filter.recipient(recipientContext);
        chain.recipient(recipientContext);
    }

    @Override
    public void data(MailData data) throws RejectException,
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
