package mireka.transmission.queuing;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import mireka.ExampleAddress;
import mireka.ExampleMail;
import mireka.transmission.Mail;
import mireka.transmission.queue.QueueStorageException;
import mireka.transmission.queue.ScheduleFileDirQueue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueuingTransmitterTest {

    @Mock
    private ScheduleFileDirQueue mockedQueue;
    private QueuingTransmitter transmitter = new QueuingTransmitter();
    private Mail mail = ExampleMail.simple();

    @Before
    public void initialize() {
        transmitter.setQueue(mockedQueue);
    }

    @Test
    public void testTransmitTwoToSameDomain() throws QueueStorageException {
        mail.recipients =
                Arrays.asList(ExampleAddress.JANE_AS_RECIPIENT,
                        ExampleAddress.JOHN_AS_RECIPIENT);
        transmitter.transmit(mail);
        verify(mockedQueue).add(any(Mail.class));
    }

    @Test
    public void testTransmitTwoToDifferentDomain() throws QueueStorageException {
        mail.recipients =
                Arrays.asList(ExampleAddress.JANE_AS_RECIPIENT,
                        ExampleAddress.NANCY_NET_AS_RECIPIENT);
        transmitter.transmit(mail);
        verify(mockedQueue, times(2)).add(any(Mail.class));
    }
}
