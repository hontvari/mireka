package mireka.transmission.queuing;

import static mireka.ExampleAddress.*;

import java.util.Arrays;

import mireka.ExampleMail;
import mireka.transmission.Mail;
import mireka.transmission.immediate.ImmediateSender;
import mireka.transmission.queue.QueueStorageException;
import mireka.transmission.queue.ScheduleFileDirQueue;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

import org.junit.Test;

public class QueuingTransmitterTest {
    @Tested
    private QueuingTransmitter transmitter;

    @Injectable
    private ScheduleFileDirQueue queue;

    @Injectable
    private ImmediateSender immediateSender;

    private final Mail mail = ExampleMail.simple();

    @Test
    public void testTransmitTwoToSameDomain() throws QueueStorageException {
        mail.recipients = Arrays.asList(JANE_AS_RECIPIENT, JOHN_AS_RECIPIENT);

        new Expectations() {
            {
                immediateSender.singleDomainOnly();
                result = true;

                queue.add((Mail)any);
            }
        };

        transmitter.transmit(mail);
    }

    @Test
    public void testTransmitTwoToDifferentDomain() throws QueueStorageException {
        mail.recipients =
                Arrays.asList(JANE_AS_RECIPIENT, NANCY_NET_AS_RECIPIENT);

        new Expectations() {
            {
                immediateSender.singleDomainOnly();
                result = true;

                queue.add((Mail) any);
                times = 2;
            }
        };

        transmitter.transmit(mail);
    }

    @Test
    public void testTransmitTwoToDifferentDomainButSmarthost()
            throws QueueStorageException {
        mail.recipients =
                Arrays.asList(JANE_AS_RECIPIENT, NANCY_NET_AS_RECIPIENT);

        new Expectations() {
            {
                immediateSender.singleDomainOnly();
                result = false;

                queue.add((Mail) any);
                times = 1;
            }
        };

        transmitter.transmit(mail);
    }

}
