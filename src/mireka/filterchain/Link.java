package mireka.filterchain;

import java.io.IOException;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

import mireka.MailData;
import mireka.filter.Filter;
import mireka.filter.FilterChain;
import mireka.filter.FilterReply;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;

class Link implements FilterChain {
    private final Filter filter;
    private final MailTransaction mailTransaction;

    public Link(Filter filter, MailTransaction mailTransaction) {
        this.filter = filter;
        this.mailTransaction = mailTransaction;
    }

    @Override
    public void begin() {
        filter.begin();
    }

    @Override
    public void from(String from) throws RejectException {
        filter.from(from);
    }

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
        return filter.verifyRecipient(recipientContext);
    }

    @Override
    public void recipient(RecipientContext recipientContext) throws RejectException {
        filter.recipient(recipientContext);
    }

    @Override
    public void data(MailData data) throws RejectException,
            TooMuchDataException, IOException {
        mailTransaction.replaceData(data);
        filter.data(data);
    }
}
