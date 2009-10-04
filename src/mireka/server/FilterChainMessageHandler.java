package mireka.server;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.inject.Initializer;
import javax.mail.internet.ParseException;

import mireka.filterchain.FilterInstances;
import mireka.mailaddress.MailAddressFactory;
import mireka.mailaddress.Recipient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.io.DeferredFileOutputStream;

public class FilterChainMessageHandler implements MessageHandler {
    private final Logger logger =
            LoggerFactory.getLogger(FilterChainMessageHandler.class);
    private final FilterInstances filterChain;
    private final MailTransactionImpl mailTransaction;

    @Initializer
    public FilterChainMessageHandler(FilterInstances filterChain,
            MailTransactionImpl mailTransactionImpl) {
        this.filterChain = filterChain;
        this.mailTransaction = mailTransactionImpl;
    }

    @Override
    public void from(String from) throws RejectException {
        filterChain.getHead().from(from);
        mailTransaction.from = from;
    }

    @Override
    public void recipient(String recipientString) throws RejectException {
        Recipient recipient = convertToRecipient(recipientString);
        filterChain.getHead().verifyRecipient(recipient);
        filterChain.getHead().recipient(recipient);
        mailTransaction.recipients.add(recipient);
    }

    private Recipient convertToRecipient(String recipient)
            throws RejectException {
        try {
            return new MailAddressFactory().createRecipient(recipient);
        } catch (ParseException e) {
            logger.debug("Syntax error in recipient " + recipient, e);
            throw new RejectException(553, "Syntax error in mailbox name "
                    + recipient);
        }
    }

    @Override
    public void data(InputStream data) throws RejectException,
            TooMuchDataException, IOException {
        DeferredFileOutputStream deferredFileOutputStream = null;
        DeferredFileMailData deferredFileMailData = null;
        try {
            deferredFileOutputStream = copyDataToDeferredFileOutputStream(data);
            deferredFileMailData =
                    new DeferredFileMailData(deferredFileOutputStream);
            mailTransaction.setData(deferredFileMailData);
            filterChain.getHead().data(mailTransaction.getData());
        } finally {
            if (deferredFileMailData != null)
                deferredFileMailData.close();
            if (deferredFileOutputStream != null)
                deferredFileOutputStream.close();
        }
    }

    private DeferredFileOutputStream copyDataToDeferredFileOutputStream(
            InputStream src) throws IOException {
        byte[] buffer = new byte[8192];
        DeferredFileOutputStream deferredFileOutputStream =
                new DeferredFileOutputStream(32768);
        int cRead;
        while ((cRead = src.read(buffer)) > 0) {
            deferredFileOutputStream.write(buffer, 0, cRead);
        }
        return deferredFileOutputStream;
    }

    @Override
    public void done() {
        filterChain.done();
    }
}
