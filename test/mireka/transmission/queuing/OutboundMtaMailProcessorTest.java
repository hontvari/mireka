package mireka.transmission.queuing;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import mireka.ExampleMail;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.immediate.ImmediateSender;
import mireka.transmission.immediate.ImmediateSenderFactory;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.SendException;
import mireka.transmission.queue.TransmitterSummary;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OutboundMtaMailProcessorTest {
    @Mock
    private ImmediateSenderFactory immediateSenderFactory;

    @Mock
    private ImmediateSender immediateSender;

    private TransmitterSummary transmitterSummary = new TransmitterSummary();

    private Mail mail = ExampleMail.simple();

    OutboundMtaMailProcessor processor;

    @Before
    public void initialize() {
        when(immediateSenderFactory.create()).thenReturn(immediateSender);
        processor =
                new OutboundMtaMailProcessor(immediateSenderFactory, null,
                        null, transmitterSummary, mail);
    }

    @Test
    public void testRun() throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException, LocalMailSystemException,
            PostponeException {
        processor.run();

        verify(immediateSender).send(any(Mail.class));
    }
}
