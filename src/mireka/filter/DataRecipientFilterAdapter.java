package mireka.filter;

import java.io.IOException;

import mireka.mailaddress.Recipient;

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
    public FilterReply verifyRecipient(Recipient recipient)
            throws RejectException {
        FilterReply result = filter.verifyRecipient(recipient);
        switch (result) {
        case ACCEPT:
            return FilterReply.ACCEPT;
        case NEUTRAL:
            return chain.verifyRecipient(recipient);
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public void recipient(Recipient recipient) throws RejectException {
        filter.recipient(recipient);
        chain.recipient(recipient);
    }

    @Override
    public void data(MailData data) throws RejectException,
            TooMuchDataException, IOException {
        filter.data(data);

        for (Recipient recipient : mailTransaction.getRecipients()) {
            filter.dataRecipient(data, recipient);
        }

        chain.data(data);
    }

    @Override
    public void done() {
        filter.done();
    }
}
