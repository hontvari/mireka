package mireka.transmission.queue;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mireka.ExampleMail;
import mireka.transmission.Mail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleFileDirQueueTest {
    @Mock
    private FileDirStore mockedStore;
    @Mock
    private ScheduledThreadPoolExecutor mockedPool;
    private ScheduleFileDirQueue queue;
    private MailName mailName = new MailName(System.currentTimeMillis(), 0);
    private Mail mail = ExampleMail.simple();

    @Before
    public void initialize() throws QueueStorageException {
        queue = new ScheduleFileDirQueue(mockedStore, null, mockedPool);
        when(mockedStore.initializeAndQueryMailNamesOrderedBySchedule())
                .thenReturn(new MailName[] { mailName });
        when(mockedStore.save(mail)).thenReturn(mailName);
    }

    @Test
    public void testStart() {
        queue.start();
        verify(mockedPool).schedule(any(Runnable.class), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    public void testAdd() throws QueueStorageException {
        mail.scheduleDate = null;
        queue.add(mail);
        verify(mockedPool).schedule(any(Runnable.class), anyLong(),
                any(TimeUnit.class));
        verify(mockedStore).save(mail);
    }
}
