package mireka.transmission.queue;

import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MailProcessingTask implements Runnable {
    private final Logger logger = LoggerFactory
            .getLogger(MailProcessingTask.class);
    private final ScheduleFileDirQueue parentQueue;
    private final MailName mailName;
    private final FileDirStore dir;
    private final MailProcessorFactory mailProcessorFactory;
    private DateTime dateOfFirstFailedAttempt = null;

    public MailProcessingTask(ScheduleFileDirQueue parentQueue,
            FileDirStore store, MailProcessorFactory mailProcessorFactory,
            MailName mailName) {
        this.parentQueue = parentQueue;
        this.dir = store;
        this.mailProcessorFactory = mailProcessorFactory;
        this.mailName = mailName;
    }

    @Override
    public void run() {
        logger.debug("Processing mail named " + mailName + "...");
        Mail mail;
        try {
            mail = dir.read(mailName);
        } catch (QueueStorageException e) {
            logger.error("Cannot read mail. Mail will remain in the queue, "
                    + "but it won't be retried until the "
                    + "next server restart. Likely it is "
                    + "best to remove it manually.", e);
            return;
        }
        MailProcessor mailProcessor = mailProcessorFactory.create(mail);
        try {
            mailProcessor.run();
        } catch (LocalMailSystemException e) {
            if (e.errorStatus().shouldRetry())
                handleTemporaryException(e);
            else
                handlePermanentException(e);
            return;
        }
        try {
            dir.delete(mailName);
        } catch (QueueStorageException e) {
            logger.error("Mail was processed sucessfully, "
                    + "but it cannot be removed. "
                    + "Mail will remain in the queue, "
                    + "it will be submitted to processing again "
                    + "following the next server restart. "
                    + "It should be deleted manually before that.", e);
            return;
        }
        logger.debug("Mail processing is completed.");
    }

    private void handleTemporaryException(LocalMailSystemException e) {
        if (dateOfFirstFailedAttempt == null)
            dateOfFirstFailedAttempt = new DateTime();
        if (taskHasBeenFailingForTooMuchTime()) {
            logger.error("A transient local failure prevented processing "
                    + "the mail. Processing of this mail is "
                    + "unsuccussful for a long time. "
                    + "The first attempt was on " + dateOfFirstFailedAttempt
                    + ". This was the last attempt, moving the mail "
                    + "to the error directory...", e);
            try {
                dir.moveToErrorDir(mailName);
                logger.debug("Mail is moved to the error directory.");
            } catch (QueueStorageException e1) {
                logger.error("Cannot move mail to error directory. "
                        + "Mail will remain in the queue, "
                        + "but it won't be retried until the "
                        + "next server restart. Likely it is "
                        + "best to remove it manually.", e1);
                return;
            }
        } else {
            logger.error("A transient local failure prevented processing "
                    + "mail. Mail will remain in the queue unmodified. "
                    + " Processing of the mail will be retried "
                    + "5 minutes later.", e);
            parentQueue.rescheduleFailedTask(this);
            return;
        }
    }

    private boolean taskHasBeenFailingForTooMuchTime() {
        DateTime deadline = dateOfFirstFailedAttempt.plusDays(1);
        return deadline.isBeforeNow();
    }

    private void handlePermanentException(LocalMailSystemException e) {
        logger.error(
                "A permanent local faulure prevented processing the mail. "
                        + "Moving the mail to the error directory...", e);
        try {
            dir.moveToErrorDir(mailName);
            logger.debug("Mail is moved to the error directory.");
        } catch (QueueStorageException e1) {
            logger.error("Cannot move mail to error directory. "
                    + "Mail will remain in the queue, "
                    + "but it won't be retried until the "
                    + "next server restart. Likely it is "
                    + "best to remove it manually.", e1);
            return;
        }
    }
}