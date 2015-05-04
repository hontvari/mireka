package mireka.transmission.queue;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import mireka.ExampleMail;
import mireka.transmission.Mail;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;

public class ScheduleFileDirQueueTest {
    @Mocked
    private FileDirStore mockedStore;
    @Mocked
    private ScheduledThreadPoolExecutor mockedPool;

    private ScheduleFileDirQueue queue;
    private MailName mailName = new MailName(System.currentTimeMillis(), 0);
    private Mail mail = ExampleMail.simple();

    @Before
    public void initialize() {
        queue = new ScheduleFileDirQueue(mockedStore, null, mockedPool);
    }

    @Test
    public void testStart() throws QueueStorageException {
        new Expectations() {
            {
                mockedStore.initializeAndQueryMailNamesOrderedBySchedule();
                result = new MailName[] { mailName };
            }
        };

        queue.start();

        new Verifications() {
            {
                mockedPool.schedule((Runnable) any, anyLong, null);
            }
        };
    }

    @Test
    public void testAdd() throws QueueStorageException {

        mail.scheduleDate = null;
        queue.add(mail);

        new Verifications() {
            {
                mockedPool.schedule((Runnable) any, anyLong, null);
                mockedStore.save(mail);
            }
        };
    }
}
