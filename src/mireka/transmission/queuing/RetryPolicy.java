package mireka.transmission.queuing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mireka.address.Recipient;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;
import mireka.transmission.dsn.DelayReport;
import mireka.transmission.dsn.DsnMailCreator;
import mireka.transmission.dsn.PermanentFailureReport;
import mireka.transmission.dsn.RecipientProblemReport;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientRejection;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.RemoteMtaErrorResponseException;
import mireka.transmission.immediate.SendException;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RetryPolicy decides what actions are necessary after a transmission attempt
 * failed and executes those actions.
 */
public class RetryPolicy {
    private final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);
    private List<Period> retryPeriods = Arrays.asList(Period.minutes(3),
            Period.minutes(27), Period.minutes(30), Period.hours(2),
            Period.hours(2), Period.hours(2), Period.hours(2), Period.hours(2),
            Period.hours(2), Period.hours(2), Period.hours(2), Period.hours(2),
            Period.hours(2), Period.hours(3));
    /**
     * Elements indicates the count of failed delivery attempts after which a
     * delayed DSN mail must be sent. For example 3 means that a DSN must be
     * issued after the third failed attempt.
     */
    private List<Integer> delayReportPoints = new ArrayList<Integer>();
    private DsnMailCreator dsnMailCreator;
    private Transmitter dsnTransmitter;
    private Transmitter retryTransmitter;

    /**
     * Constructs a new empty instance, required attributes must be passed using
     * the setter methods later.
     */
    public RetryPolicy() {
        // nothing to do
    }

    /**
     * Constructs a new instance with all required dependencies.
     */
    public RetryPolicy(DsnMailCreator dsnMailCreator,
            Transmitter dsnTransmitter, Transmitter retryTransmitter) {
        this.dsnMailCreator = dsnMailCreator;
        this.dsnTransmitter = dsnTransmitter;
        this.retryTransmitter = retryTransmitter;
    }

    /**
     * @throws LocalMailSystemException
     *             if a bounce (DSN) mail cannot be created or passed to a queue
     */
    public void actOnEntireMailFailure(Mail mail, SendException exception)
            throws LocalMailSystemException {
        EntireMailFailureHandler failureHandler =
                new EntireMailFailureHandler(mail, exception);
        failureHandler.onFailure();
    }

    /**
     * @throws LocalMailSystemException
     *             if a bounce (DSN) mail cannot be created or passed to a queue
     */
    public void actOnRecipientsWereRejected(Mail mail,
            RecipientsWereRejectedException exception)
            throws LocalMailSystemException {
        RecipientsRejectedFailureHandler failureHandler =
                new RecipientsRejectedFailureHandler(mail, exception.rejections);
        failureHandler.onFailure();
    }

    public void actOnPostponeRequired(Mail mail, PostponeException e)
            throws LocalMailSystemException {
        mail.postpones++;
        if (mail.postpones <= 3) {
            Instant newScheduleDate =
                    new DateTime().plusSeconds(e.getRecommendedDelay())
                            .toInstant();
            mail.scheduleDate = newScheduleDate.toDate();
            retryTransmitter.transmit(mail);
            logger.debug("Delivery must be postponed to all hosts. "
                    + "Rescheduling the attempt. This is the " + mail.postpones
                    + ". postponing of this delivery attempt.");

        } else {
            logger.debug("Too much postponings of delivery attempt. "
                    + "The next would be the " + mail.postpones
                    + ". Attempt is considered to be a failure.");
            SendException sendException =
                    new SendException(
                            "Too much postponings of delivery attempt, attempt is considered to be a failure.",
                            e, e.getEnhancedStatus(), e.getRemoteMta());
            EntireMailFailureHandler failureHandler =
                    new EntireMailFailureHandler(mail, sendException);
            failureHandler.onFailure();
        }
    }

    private int maxAttempts() {
        return retryPeriods.size();
    }

    /**
     * @category GETSET
     */
    public void setRetryPeriods(List<Period> retryPeriods) {
        this.retryPeriods = retryPeriods;
    }

    /**
     * @category GETSET
     */
    public void setDelayReportPoints(List<Integer> delayReportPoints) {
        this.delayReportPoints.clear();
        this.delayReportPoints.addAll(delayReportPoints);
    }

    /**
     * @category GETSET
     */
    public void setDelayReportPoint(int index) {
        this.delayReportPoints.clear();
        this.delayReportPoints.add(index);
    }

    /**
     * @category GETSET
     */
    public void setDsnMailCreator(DsnMailCreator dsnMailCreator) {
        this.dsnMailCreator = dsnMailCreator;
    }

    /**
     * @category GETSET
     */
    public void setDsnTransmitter(Transmitter dsnTransmitter) {
        this.dsnTransmitter = dsnTransmitter;
    }

    /**
     * @category GETSET
     */
    public void setRetryTransmitter(Transmitter retryTransmitter) {
        this.retryTransmitter = retryTransmitter;
    }

    private class RecipientsRejectedFailureHandler extends FailureHandler {
        private final List<RecipientRejection> rejections;

        public RecipientsRejectedFailureHandler(Mail mail,
                List<RecipientRejection> rejections) {
            super(mail);
            this.rejections = rejections;
        }

        @Override
        protected List<SendingFailure> createFailures() {
            List<SendingFailure> result = new ArrayList<SendingFailure>();
            for (RecipientRejection rejection : rejections) {
                result.add(new SendingFailure(rejection.recipient,
                        rejection.sendException));
            }
            return result;
        }
    }

    private class EntireMailFailureHandler extends FailureHandler {

        private final SendException sendException;

        public EntireMailFailureHandler(Mail mail, SendException sendException) {
            super(mail);
            this.sendException = sendException;
        }

        @Override
        protected List<SendingFailure> createFailures() {
            List<SendingFailure> result = new ArrayList<SendingFailure>();
            for (Recipient recipient : mail.recipients) {
                result.add(new SendingFailure(recipient, sendException));
            }
            return result;
        }
    }

    private abstract class FailureHandler {
        private final Logger logger = LoggerFactory
                .getLogger(EntireMailFailureHandler.class);
        protected final Mail mail;

        private List<SendingFailure> failures;
        private List<SendingFailure> permanentFailures =
                new ArrayList<SendingFailure>();
        private List<SendingFailure> transientFailures =
                new ArrayList<SendingFailure>();
        private List<PermanentFailureReport> permanentFailureReports =
                new ArrayList<PermanentFailureReport>();
        private List<DelayReport> delayReports = new ArrayList<DelayReport>();

        public FailureHandler(Mail mail) {
            this.mail = mail;
        }

        public final void onFailure() throws LocalMailSystemException {
            mail.deliveryAttempts++;
            mail.postpones = 0;
            failures = createFailures();
            separatePermanentAndTemporaryFailures();
            createPermanentFailureReports();
            createDelayReports();
            sendDsnMail();
            rescheduleTemporaryFailures();
        }

        protected abstract List<SendingFailure> createFailures();

        private void separatePermanentAndTemporaryFailures() {
            for (SendingFailure failure : failures) {
                if (failure.exception.errorStatus().shouldRetry())
                    transientFailures.add(failure);
                else
                    permanentFailures.add(failure);
            }
            if (mail.deliveryAttempts > maxAttempts()
                    && !transientFailures.isEmpty()) {
                logger.debug("Giving up after the " + mail.deliveryAttempts
                        + ". transient failure. Considering it as "
                        + "a permanent failure.");
                permanentFailures.addAll(transientFailures);
                transientFailures.clear();
                return;
            }
        }

        private void createPermanentFailureReports() {
            for (SendingFailure failure : permanentFailures) {
                PermanentFailureReport report = new PermanentFailureReport();
                fillInRecipientFailureReport(report, failure);
                permanentFailureReports.add(report);
            }
        }

        private void fillInRecipientFailureReport(
                RecipientProblemReport report, SendingFailure failure) {
            SendException exception = failure.exception;
            report.recipient = failure.recipient;
            report.status = exception.errorStatus();
            report.remoteMta = exception.remoteMta();
            if (exception instanceof RemoteMtaErrorResponseException)
                report.remoteMtaDiagnosticStatus =
                        ((RemoteMtaErrorResponseException) exception)
                                .remoteMtaStatus();
            report.failureDate = exception.failureDate;
            report.logId = exception.getLogId();
        }

        private void createDelayReports() {
            if (!delayReportPoints.contains(mail.deliveryAttempts))
                return;
            for (SendingFailure failure : transientFailures) {
                DelayReport report = new DelayReport();
                fillInRecipientFailureReport(report, failure);
                delayReports.add(report);
            }
        }

        private void sendDsnMail() throws LocalMailSystemException {
            List<RecipientProblemReport> reports =
                    new ArrayList<RecipientProblemReport>();
            reports.addAll(permanentFailureReports);
            reports.addAll(delayReports);

            if (reports.isEmpty())
                return;
            if (mail.from.isNull()) {
                logger.debug("Failure or delay, but reverse-path is null, "
                        + "DSN must not be sent. "
                        + "Original mail itself was a notification.");
                return;
            }
            Mail dsnMail = dsnMailCreator.create(mail, reports);
            dsnTransmitter.transmit(dsnMail);
            logger.debug("DSN message is created with "
                    + permanentFailureReports.size()
                    + " permanent failures and " + delayReports.size()
                    + " delays and passed to the DSN transmitter.");
        }

        private void rescheduleTemporaryFailures()
                throws LocalMailSystemException {
            if (transientFailures.isEmpty())
                return;
            Period waitingPeriod = retryPeriods.get(mail.deliveryAttempts - 1);
            Instant newScheduleDate =
                    new DateTime().plus(waitingPeriod).toInstant();
            mail.scheduleDate = newScheduleDate.toDate();
            mail.recipients = calculateTemporarilyRejectedRecipientList();
            retryTransmitter.transmit(mail);
            logger.debug("Transient failure, the mail is scheduled for a "
                    + (mail.deliveryAttempts + 1) + ". attempt "
                    + waitingPeriod + " later on " + newScheduleDate);
        }

        private List<Recipient> calculateTemporarilyRejectedRecipientList() {
            List<Recipient> result = new ArrayList<Recipient>();
            for (SendingFailure failure : transientFailures) {
                result.add(failure.recipient);
            }
            return result;
        }
    }

    /**
     * SendingFailure stores failure information for a specific recipient.
     */
    private static class SendingFailure {
        public final Recipient recipient;
        public final SendException exception;

        public SendingFailure(Recipient recipient, SendException exception) {
            this.recipient = recipient;
            this.exception = exception;
        }
    }
}
