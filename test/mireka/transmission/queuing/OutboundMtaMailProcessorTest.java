package mireka.transmission.queuing;

import mireka.ExampleMail;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.immediate.ImmediateSender;
import mireka.transmission.immediate.ImmediateSenderFactory;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.SendException;
import mireka.transmission.queue.TransmitterSummary;
import mockit.Cascading;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Before;
import org.junit.Test;

public class OutboundMtaMailProcessorTest {
    @Mocked
    private ImmediateSenderFactory immediateSenderFactory;

    @Mocked
    private ImmediateSender immediateSender;

    @Cascading
    private TransmitterSummary transmitterSummary;

    private Mail mail = ExampleMail.simple();

    OutboundMtaMailProcessor processor;

    @Before
    public void initialize() {
        processor =
                new OutboundMtaMailProcessor(immediateSenderFactory, null,
                        null, transmitterSummary, mail);
    }

    @Test
    public void testRun() throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException, LocalMailSystemException,
            PostponeException {
        new Expectations() {
            {
                immediateSenderFactory.create();
                result = immediateSender;

                immediateSender.send((Mail) any);
            }
        };

        processor.run();
    }
}
