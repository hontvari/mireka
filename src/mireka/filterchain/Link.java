package mireka.filterchain;

import java.io.IOException;

import mireka.address.ReversePath;
import mireka.filter.Filter;
import mireka.filter.FilterChain;
import mireka.filter.FilterReply;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.maildata.MaildataFile;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

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
    public void from(ReversePath from) throws RejectExceptionExt {
        filter.from(from);
    }

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        return filter.verifyRecipient(recipientContext);
    }

    @Override
    public void recipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        filter.recipient(recipientContext);
    }

    @Override
    public void data(MaildataFile data) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        mailTransaction.replaceData(data);
        filter.data(data);
    }
}
