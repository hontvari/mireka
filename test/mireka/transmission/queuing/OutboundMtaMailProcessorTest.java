package mireka.transmission.queuing;

import mireka.ExampleMail;
import mireka.smtp.SendException;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.immediate.ImmediateSender;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.queue.TransmitterSummary;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Before;
import org.junit.Test;

public class OutboundMtaMailProcessorTest {
    @Mocked
    private ImmediateSender immediateSender;

    @Mocked
    private TransmitterSummary transmitterSummary;

    private final Mail mail = ExampleMail.simple();

    OutboundMtaMailProcessor processor;

    @Before
    public void initialize() {
        processor =
                new OutboundMtaMailProcessor(immediateSender, null, null,
                        transmitterSummary, mail);
    }

    @Test
    public void testRun() throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException, LocalMailSystemException,
            PostponeException {
        new Expectations() {
            {
                immediateSender.send((Mail) any);
            }
        };

        processor.run();
    }
}
