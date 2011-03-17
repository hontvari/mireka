package mireka.transmission.queuing;

import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.immediate.ImmediateSender;
import mireka.transmission.immediate.ImmediateSenderFactory;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.SendException;
import mireka.transmission.queue.MailProcessor;
import mireka.transmission.queue.TransmitterSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OutboundMtaMailProcessor implements MailProcessor {
    private final Logger logger = LoggerFactory
            .getLogger(OutboundMtaMailProcessor.class);
    private final ImmediateSenderFactory immediateSenderFactory;
    private final RetryPolicy retryPolicy;
    private final LogIdFactory logIdFactory;
    private final Mail mail;
    private final TransmitterSummary summary;

    public OutboundMtaMailProcessor(
            ImmediateSenderFactory immediateSenderFactory,
            RetryPolicy retryPolicy, LogIdFactory logIdFactory,
            TransmitterSummary summary, Mail mail) {
        this.immediateSenderFactory = immediateSenderFactory;
        this.mail = mail;
        this.retryPolicy = retryPolicy;
        this.logIdFactory = logIdFactory;
        this.summary = summary;
    }

    @Override
    public void run() throws LocalMailSystemException {
        try {
            send();
        } catch (Throwable e) {
            logger.error("Abandoning " + mail + " after unexpected exception. "
                    + "You may have to correct mail stores manually.", e);
            summary.lastError = e.toString();
            summary.errors.incrementAndGet();
        }

    }

    private void send() throws LocalMailSystemException {
        ImmediateSender sender = immediateSenderFactory.create();
        try {
            logger.debug("Sending mail " + mail + "...");
            summary.mailTransactions.incrementAndGet();

            sender.send(mail);

            logger.debug("Sent successfully");
            summary.successfulMailTransactions.incrementAndGet();
        } catch (SendException e) {
            handleSendException(e);
        } catch (RecipientsWereRejectedException e) {
            handleSomeRecipientsWereRejectedException(e);
        } catch (PostponeException e) {
            handlePostponeException(e);
        }
    }

    private void handleSendException(SendException e)
            throws LocalMailSystemException {
        String logId = logIdFactory.next();
        e.initLogId(logId);

        logger.debug("Send failed. Log-ID=" + logId
                + ". Executing retry policy...", e);
        summary.failures.incrementAndGet();
        summary.lastFailure = e.toString();
        increaseTransientOrPermanentFailureCount(e);

        retryPolicy.actOnEntireMailFailure(mail, e);
    }

    private void handleSomeRecipientsWereRejectedException(
            RecipientsWereRejectedException e) throws LocalMailSystemException {
        if (mail.recipients.size() == 1) {
            logger.debug("The single recipient was rejected. "
                    + "Executing retry policy...");
            summary.failures.incrementAndGet();
        } else if (mail.recipients.size() == e.rejections.size()) {
            logger.debug("All " + mail.recipients.size()
                    + " recipients were rejected. "
                    + "Executing retry policy...");
            summary.failures.incrementAndGet();
        } else {
            logger.debug("Some, but not all recipients were rejected. "
                    + "Executing retry policy...");
            summary.partialFailures.incrementAndGet();
        }
        increaseTransientOrPermanentFailureCount(e.rejections.get(0).sendException);
        summary.lastFailure = e.toString();

        retryPolicy.actOnRecipientsWereRejected(mail, e);
    }

    private void increaseTransientOrPermanentFailureCount(SendException e) {
        if (e.errorStatus().shouldRetry())
            summary.transientFailures.incrementAndGet();
        else
            summary.permanentFailures.incrementAndGet();
    }

    private void handlePostponeException(PostponeException e)
            throws LocalMailSystemException {
        logger.debug("Delivery must be postponed to all hosts. "
                + "Executing retry policy...");
        retryPolicy.actOnPostponeRequired(mail, e);
    }
}