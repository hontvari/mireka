package mireka.filterchain;

import java.io.IOException;
import java.io.InputStream;

import mireka.address.ReversePath;
import mireka.filter.FilterChain;
import mireka.filter.FilterReply;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.maildata.Maildata;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * ChainEnd is the closing element of the filter chain, it follows the last
 * {@link Link}, which is associated with the last filter.
 */
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
    public void dataStream(InputStream in) throws RejectExceptionExt,
            TooMuchDataException, IOException {
        // do nothing
    }

    @Override
    public void data(Maildata data) throws RejectException,
            TooMuchDataException, IOException {
        mailTransaction.replaceData(data);
    }
}
