package mireka.transmission.queue;

import java.util.Date;

import mireka.smtp.EnhancedStatus;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

public class MailProcessingTaskTest {

    @Mocked
    private ScheduleFileDirQueue mockedQueue;
    @Mocked
    private FileDirStore mockedStore;
    @Mocked
    private MailProcessorFactory mockedMailProcessorFactory;
    @Mocked
    private MailProcessor mockedMailProcessor;

    private MailProcessingTask task;
    private MailName mailName = new MailName(new Date().getTime(), 0);

    @Before
    public void initialize() {
        task =
                new MailProcessingTask(mockedQueue, mockedStore,
                        mockedMailProcessorFactory, mailName);

        new Expectations() {
            {
                mockedMailProcessorFactory.create((Mail) any);
                result = mockedMailProcessor;
            }
        };
    }

    @Test
    public void testRunSuccessful() throws QueueStorageException {
        task.run();

        new Verifications() {
            {
                mockedStore.delete(mailName);
            }
        };
    }

    @Test
    public void testRunLocalTransientFailure() throws LocalMailSystemException {
        new Expectations() {
            {
                mockedMailProcessor.run();
                result =
                        new QueueStorageException(
                                EnhancedStatus.MAIL_SYSTEM_FULL);
            }
        };

        task.run();

        new Verifications() {
            {
                mockedQueue.rescheduleFailedTask(task);
                mockedStore.delete(mailName);
                times = 0;
            }
        };
    }

    @Test
    public void testRunLocalTransientFailureForTooLong()
            throws LocalMailSystemException {
        new Expectations() {
            {
                mockedMailProcessor.run();
                result =
                        new QueueStorageException(
                                EnhancedStatus.MAIL_SYSTEM_FULL);
            }
        };

        task.run(); // first attempt
        long twoDaysLater = new DateTime().plusDays(2).getMillis();
        DateTimeUtils.setCurrentMillisFixed(twoDaysLater);
        task.run(); // second attempt, now it is too late for another attempt
        DateTimeUtils.setCurrentMillisSystem();

        new Verifications() {
            {
                mockedQueue.rescheduleFailedTask(task);
                times = 1;

                mockedStore.moveToErrorDir(mailName);
            }
        };
    }

    @Test
    public void testRunLocalPermanentFailure() throws LocalMailSystemException {
        new Expectations() {
            {
                mockedMailProcessor.run();
                result =
                        new QueueStorageException(
                                EnhancedStatus.PERMANENT_INTERNAL_ERROR);
            }
        };

        task.run();

        new Verifications() {
            {
                mockedStore.moveToErrorDir(mailName);

                mockedQueue.rescheduleFailedTask(task);
                times = 0;
            }
        };
    }

}
