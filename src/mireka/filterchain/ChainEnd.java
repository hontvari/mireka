package mireka.filterchain;

import java.io.IOException;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

import mireka.UnknownUserException;
import mireka.filter.FilterChain;
import mireka.filter.FilterReply;
import mireka.filter.MailData;
import mireka.filter.MailTransaction;
import mireka.mailaddress.Recipient;

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
    public void from(String from) {
        // do nothing
    }

    @Override
    public FilterReply verifyRecipient(Recipient recipient)
            throws RejectException {
        throw new UnknownUserException(recipient);
    }

    @Override
    public void recipient(Recipient recipient) throws RejectException {
        // do nothing
    }

    @Override
    public void data(MailData data) throws RejectException,
            TooMuchDataException, IOException {
        mailTransaction.replaceData(data);
    }
}
