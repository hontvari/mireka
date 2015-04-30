package mireka.filterchain;

import java.io.IOException;

import mireka.address.ReversePath;
import mireka.filter.FilterChain;
import mireka.filter.FilterReply;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.maildata.MaildataFile;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

class ChainEnd implements FilterChain {
    private final MailTransaction mailTransaction;

    public ChainEnd(MailTransaction mailTransaction) {
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
            throws RejectException {
        // do nothing
    }

    @Override
    public void data(MaildataFile data) throws RejectException,
            TooMuchDataException, IOException {
        mailTransaction.replaceData(data);
    }
}
