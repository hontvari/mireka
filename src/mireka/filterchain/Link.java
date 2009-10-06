package mireka.filterchain;

import java.io.IOException;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

import mireka.filter.Filter;
import mireka.filter.FilterChain;
import mireka.filter.FilterReply;
import mireka.filter.MailData;
import mireka.filter.MailTransaction;
import mireka.mailaddress.Recipient;

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
    public void from(String from) {
        filter.from(from);
    }

    @Override
    public FilterReply verifyRecipient(Recipient recipient)
            throws RejectException {
        return filter.verifyRecipient(recipient);
    }

    @Override
    public void recipient(Recipient recipient) throws RejectException {
        filter.recipient(recipient);
    }

    @Override
    public void data(MailData data) throws RejectException,
            TooMuchDataException, IOException {
        mailTransaction.replaceData(data);
        filter.data(data);
    }
}
