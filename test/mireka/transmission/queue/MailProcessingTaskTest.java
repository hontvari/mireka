package mireka.transmission.queue;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import mireka.smtp.EnhancedStatus;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MailProcessingTaskTest {

    @Mock
    private ScheduleFileDirQueue mockedQueue;
    @Mock
    private FileDirStore mockedStore;
    @Mock
    private MailProcessorFactory mockedMailProcessorFactory;
    @Mock
    private MailProcessor mockedMailProcessor;
    private MailProcessingTask task;
    private MailName mailName = MailName.create(new Date());

    @Before
    public void initialize() {
        task =
                new MailProcessingTask(mockedQueue, mockedStore,
                        mockedMailProcessorFactory, mailName);
        when(mockedMailProcessorFactory.create(any(Mail.class))).thenReturn(
                mockedMailProcessor);
    }

    @Test
    public void testRunSuccessful() throws QueueStorageException {
        task.run();
        verify(mockedStore).delete(mailName);
    }

    @Test
    public void testRunLocalTransientFailure() throws LocalMailSystemException {
        doThrow(new QueueStorageException(EnhancedStatus.MAIL_SYSTEM_FULL))
                .when(mockedMailProcessor).run();
        task.run();
        verify(mockedQueue).rescheduleFailedTask(task);
        verify(mockedStore, Mockito.never()).delete(mailName);
    }

    @Test
    public void testRunLocalTransientFailureForTooLong()
            throws LocalMailSystemException {
        doThrow(new QueueStorageException(EnhancedStatus.MAIL_SYSTEM_FULL))
                .when(mockedMailProcessor).run();

        task.run(); // first attempt
        long twoDaysLater = new DateTime().plusDays(2).getMillis();
        DateTimeUtils.setCurrentMillisFixed(twoDaysLater);
        task.run(); // second attempt, now it is too late for another attempt
        DateTimeUtils.setCurrentMillisSystem();

        verify(mockedQueue, times(1)).rescheduleFailedTask(task);
        verify(mockedStore).moveToErrorDir(mailName);
    }

    @Test
    public void testRunLocalPermanentFailure() throws LocalMailSystemException {
        doThrow(
                new QueueStorageException(
                        EnhancedStatus.PERMANENT_INTERNAL_ERROR)).when(
                mockedMailProcessor).run();
        task.run();
        verify(mockedStore).moveToErrorDir(mailName);
        verify(mockedQueue, never()).rescheduleFailedTask(task);
    }

}
