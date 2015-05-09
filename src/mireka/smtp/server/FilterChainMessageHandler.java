package mireka.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.ConfigurationException;
import mireka.destination.UnknownRecipientDestination;
import mireka.filter.Filter;
import mireka.filter.FilterSession;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.maildata.Maildata;
import mireka.maildata.io.MaildataFileReadException;
import mireka.maildata.io.TmpMaildataFile;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.UnknownUserException;
import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.ReversePath;
import mireka.util.StreamCopier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * FilterChainMessageHandler is a <code>MessageHandler</code> which passes all
 * mail transaction events to a filter chain. The filters in the chain are
 * responsible among others for the final delivery of the mail.
 */
public class FilterChainMessageHandler implements MessageHandler {
    private final Logger logger = LoggerFactory
            .getLogger(FilterChainMessageHandler.class);
    private final MailTransaction transaction;
    private final List<FilterSession> sessions = new ArrayList<>();
    private FilterSession head;

    public FilterChainMessageHandler(MessageContext ctx, List<Filter> filters) {
        this.transaction = new MailTransaction(ctx);
        setupChain(filters);
    }

    /**
     * Sets up the filter session chain. It does not call the <code>begin</code>
     * function of the filters, because if an error happens that can be better
     * handled in the <code>from</code> function of this object.
     */
    private void setupChain(List<Filter> filters) {
        // create links and link them together
        FilterSession nextLink = new ChainEnd();
        for (int i = filters.size() - 1; i >= 0; i--) {
            FilterSession session = filters.get(i).createSession();
            session.setNextLink(nextLink);
            session.setMailTransaction(transaction);
            sessions.add(session);
            nextLink = session;
        }
        head = nextLink;
    }

    @Override
    public void from(String from) throws RejectException {
        callBeginOnFilters();

        try {
            transaction.reversePath = convertToReversePath(from);
            head.from();
        } catch (RejectExceptionExt e) {
            throw e.toRejectException();
        }
    }

    private void callBeginOnFilters() {
        for (FilterSession filterSession : sessions) {
            filterSession.begin();
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
                    new RecipientContext(transaction, recipient);
            RecipientVerificationResult filterReply =
                    head.verifyRecipient(recipientContext);
            if (filterReply == RecipientVerificationResult.NEUTRAL) {
                if (!recipientContext.isDestinationAssigned()
                        || (recipientContext.getDestination() instanceof UnknownRecipientDestination))
                    throw new UnknownUserException(recipientContext.recipient);
            }

            head.recipient(recipientContext);
            transaction.recipientContexts.add(recipientContext);
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
        transaction.dataStream = new SmtpDataInputStream(data);
        head.dataStream();

        try (TmpMaildataFile tmpMaildataFile = new TmpMaildataFile()) {

            try (OutputStream tmpOut =
                    tmpMaildataFile.deferredFile.getOutputStream()) {
                StreamCopier.writeInputStreamIntoOutputStream(
                        transaction.dataStream, tmpOut);
            }
            try (Maildata maildata = new Maildata(tmpMaildataFile)) {
                transaction.data = maildata;
                head.data();
                checkResponsibilityHasBeenTakenForAllRecipients();
            } catch (MaildataFileReadException e) {
                // this hides the real checked exception, rethrow the real one
                throw e.ioExceptionCause;
            }
        } catch (RejectExceptionExt e) {
            throw e.toRejectException();
        } catch (TooMuchDataException e) {
            logger.debug("SMTP maildata stream is too long");
            throw new RejectExceptionExt(EnhancedStatus.MESSAGE_TOO_BIG)
                    .toRejectException();
        } catch (SmtpDataReadException e) {
            logger.error(
                    "Network error while reading maildata after SMTP DATA command.",
                    e);
            throw e.ioExceptionCause;
        } finally {
            // Maildata may be replaced with another Maildata by a filter.
            if (transaction.data != null)
                transaction.data.close();
        }
    }

    private void checkResponsibilityHasBeenTakenForAllRecipients()
            throws ConfigurationException {
        for (RecipientContext recipientContext : transaction.recipientContexts) {
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

    /**
     * {@inheritDoc}
     * 
     * Calls done method of all filters even if one or more fails.
     */
    @Override
    public void done() {
        for (FilterSession filter : sessions) {
            try {
                filter.done();
            } catch (RuntimeException e) {
                logger.error("Exception in done method of filter. "
                        + "done method of other filters will still run.", e);
            }
        }
    }
}
