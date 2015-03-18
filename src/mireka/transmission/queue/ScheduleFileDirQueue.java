package mireka.transmission.queue;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import mireka.transmission.Mail;

import org.slf4j.LoggerFactory;

/**
 * This mail queue, working with a mail store, passes the mails to a mail
 * processors according to the schedule. The schedule is defined by the time
 * point in {@link Mail#scheduleDate} in each mail.
 */
public class ScheduleFileDirQueue {
    private final org.slf4j.Logger logger = LoggerFactory
            .getLogger(ScheduleFileDirQueue.class);
    private FileDirStore store;
    private MailProcessorFactory mailProcessorFactory;
    private ScheduledThreadPoolExecutor executor;

    /**
     * use this constructor with setters
     */
    public ScheduleFileDirQueue() {
        // nothing to do
    }

    /**
     * @param executor
     *            {@link ScheduledThreadPoolExecutor#setExecuteExistingDelayedTasksAfterShutdownPolicy}
     *            will be called on it with false, to switch off waiting for
     *            tasks which are not even started on shutdown.
     */
    public ScheduleFileDirQueue(FileDirStore store,
            MailProcessorFactory mailProcessorFactory,
            ScheduledThreadPoolExecutor executor) {
        this.store = store;
        this.mailProcessorFactory = mailProcessorFactory;
        this.executor = executor;
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    @PostConstruct
    public void start() {
        logger.info("Initializing queue. [store=" + store + "]");
        MailName[] mailNames;
        try {
            mailNames = store.initializeAndQueryMailNamesOrderedBySchedule();
        } catch (QueueStorageException e) {
            throw new RuntimeException("Queue initialization failed", e);
        }
        scheduleMailNames(mailNames);
    }

    private void scheduleMailNames(MailName[] mailNames) {
        for (MailName name : mailNames) {
            scheduleMailName(name);
            logger.debug(
                    "Mail name, read from store, was scheduled for processing: {}",
                    name);
        }
    }

    private void scheduleMailName(MailName mailName) {
        MailProcessingTask task =
                new MailProcessingTask(this, store, mailProcessorFactory,
                        mailName);
        executor.schedule(task,
                mailName.scheduleDate - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * store a copy of the mail in the queue and schedule it
     */
    public void add(Mail srcMail) throws QueueStorageException {
        if (srcMail.scheduleDate == null)
            srcMail.scheduleDate = new Date();
        MailName mailName = store.save(srcMail);
        scheduleMailName(mailName);
        logger.debug("Mail was sceduled for processing: {}, {}", mailName,
                srcMail);
    }

    /**
     * Initiates an orderly shutdown, no new mails will be accepted and
     * processing of mails which are not yet started will not be started
     */
    public void shutdown() {
        executor.shutdown();
    }

    void rescheduleFailedTask(MailProcessingTask task) {
        executor.schedule(task, 5, TimeUnit.MINUTES);
    }

    /**
     * @x.category GETSET
     */
    public void setStore(FileDirStore store) {
        this.store = store;
    }

    /**
     * @x.category GETSET
     */
    public void setMailProcessorFactory(
            MailProcessorFactory mailProcessorFactory) {
        this.mailProcessorFactory = mailProcessorFactory;
    }

    /**
     * @x.category GETSET
     */
    public void setExecutor(ScheduledThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * @x.category GETSET
     */
    public void setThreadCount(int count) {
        this.executor = new ScheduledThreadPoolExecutor(count);
    }

    @Override
    public String toString() {
        return "ScheduleFileDirQueue [store=" + store + "]";
    }
}
