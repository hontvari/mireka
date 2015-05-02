package mireka.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

import mireka.ConfigurationException;
import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.address.ReversePath;
import mireka.destination.UnknownRecipientDestination;
import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filterchain.FilterInstances;
import mireka.maildata.Maildata;
import mireka.maildata.io.TmpMaildataFile;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.UnknownUserException;
import mireka.util.StreamCopier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class FilterChainMessageHandler implements MessageHandler {
    private final Logger logger = LoggerFactory
            .getLogger(FilterChainMessageHandler.class);
    private final FilterInstances filterChain;
    private final MailTransactionImpl mailTransaction;

    public FilterChainMessageHandler(FilterInstances filterChain,
            MailTransactionImpl mailTransactionImpl) {
        this.filterChain = filterChain;
        this.mailTransaction = mailTransactionImpl;
    }

    @Override
    public void from(String from) throws RejectException {
        try {
            ReversePath reversePath = convertToReversePath(from);
            filterChain.getHead().from(reversePath);
            mailTransaction.from = from;
        } catch (RejectExceptionExt e) {
            throw e.toRejectException();
        }
    }

    private ReversePath convertToReversePath(String reversePath)
            throws RejectException {
        try {
            return new MailAddressFactory().createReversePath(reversePath);
        } catch (ParseException e) {
            logger.debug("Syntax error in reverse path " + reversePath, e);
            throw new RejectException(553, "Syntax error in reverse path "
                    + reversePath);
        }
    }

    @Override
    public void recipient(String recipientString) throws RejectException {
        try {
            Recipient recipient = convertToRecipient(recipientString);
            RecipientContext recipientContext =
                    new RecipientContext(mailTransaction, recipient);
            FilterReply filterReply =
                    filterChain.getHead().verifyRecipient(recipientContext);
            if (filterReply == FilterReply.NEUTRAL) {
                if (!recipientContext.isDestinationAssigned()
                        || (recipientContext.getDestination() instanceof UnknownRecipientDestination))
                    throw new UnknownUserException(recipientContext.recipient);
            }
            filterChain.getHead().recipient(recipientContext);
            mailTransaction.recipientContexts.add(recipientContext);
        } catch (RejectExceptionExt e) {
            throw e.toRejectException();
        }
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
        try (TmpMaildataFile tmpMaildataFile = new TmpMaildataFile()) {
            filterChain.getHead().dataStream(data);

            try (OutputStream tmpOut =
                    tmpMaildataFile.deferredFile.getOutputStream()) {
                StreamCopier.writeInputStreamIntoOutputStream(data, tmpOut);
            }
            try (Maildata maildata = new Maildata(tmpMaildataFile)) {
                mailTransaction.setData(maildata);
                filterChain.getHead().data(mailTransaction.getData());
                checkResponsibilityHasBeenTakenForAllRecipients();
            }
        } catch (RejectExceptionExt e) {
            throw e.toRejectException();
        } finally {
            // Maildata may be replaced with another Maildata by a filter.
            if (mailTransaction.getData() != null)
                mailTransaction.getData().close();
        }
    }

    private void checkResponsibilityHasBeenTakenForAllRecipients()
            throws ConfigurationException {
        for (RecipientContext recipientContext : mailTransaction.recipientContexts) {
            if (!recipientContext.isResponsibilityTransferred) {
                throw new ConfigurationException("Processing of mail data "
                        + "completed, but no filter has took the "
                        + "responsibility for the recipient "
                        + recipientContext.recipient + ", "
                        + "whose assigned destination was "
                        + recipientContext.getDestination());
            }
        }
    }

    @Override
    public void done() {
        filterChain.done();
    }
}
