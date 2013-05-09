package mireka.transmission.immediate;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import mireka.ExampleMail;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.SendException;
import mireka.smtp.client.MtaAddress;
import mireka.smtp.client.SmtpClient;
import mireka.transmission.Mail;
import mireka.transmission.immediate.host.MailToHostTransmitter;
import mireka.transmission.immediate.host.OutgoingConnectionsRegistry;
import mireka.transmission.queuing.LogIdFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient;
import org.subethamail.smtp.client.SMTPException;

public class MailToHostTransmitterTest {
    @Tested
    private MailToHostTransmitter sender;
    
    @Injectable
    private LogIdFactory logIdFactory;

    @Injectable
    private OutgoingConnectionsRegistry outgoingConnectionsRegistry;
    
    @Mocked
    private SmtpClient client;

    
    private static final PostponeException POSTPONE_EXCEPTION =
            new PostponeException(
                    10,
                    EnhancedStatus.TRANSIENT_SYSTEM_NOT_ACCEPTING_NETWORK_MESSAGES,
                    "Test exception");

    private static final SMTPException SMTP_EXCEPTION = new SMTPException(
            new SMTPClient.Response(500, "Test error"));


    private final Mail mail = ExampleMail.simple();

    @Before
    public void beforeTest() {
        new NonStrictExpectations() {{
            client.getMtaAddress();
            result = new MtaAddress(HOST1_EXAMPLE_COM, IP1);
        }};
        
    }
    
    @Test
    public void testSend() throws SendException,
            RecipientsWereRejectedException, IOException, PostponeException {

        sender.transmit(mail, client);

        new Verifications() {
            {
                client.from(anyString);
                client.to(anyString);
                client.dataEnd();
                client.quit();
            }
        };

    }

    @Test
    public void testSendAllAddressRejected() throws IllegalArgumentException,
            SendException, SMTPException, IOException, PostponeException {
        new NonStrictExpectations() {
            {
                client.to(anyString);
                result = SMTP_EXCEPTION;

            }
        };

        try {
            sender.transmit(mail, client);
            fail("Exception must have been thrown");
        } catch (RecipientsWereRejectedException e) {
            assertEquals(e.rejections.size(), 1);
            assertEquals(e.rejections.get(0).recipient, JANE_AS_RECIPIENT);
        }

        new Verifications() {
            {
                client.dataStart();
                times = 0;
            }
        };
    }

    @Test
    public void testSendFirstAddressRejectedFromTwo()
            throws IllegalArgumentException, SendException, SMTPException,
            IOException, PostponeException {

        new NonStrictExpectations() {
            {
                client.to(anyString);
                result = SMTP_EXCEPTION;
                result = null;
            }
        };

        try {
            mail.recipients =
                    Arrays.asList(JANE_AS_RECIPIENT, JOHN_AS_RECIPIENT);
            sender.transmit(mail, client);
            fail("Exception must have been thrown");
        } catch (RecipientsWereRejectedException e) {
            assertEquals(1, e.rejections.size());
            assertEquals(JANE_AS_RECIPIENT, e.rejections.get(0).recipient);
        }

        new Verifications() {
            {
                client.dataStart();
            }
        };
    }

    @Test
    public void testSendIoExceptionIsTransient() throws SMTPException,
            IOException, RecipientsWereRejectedException, PostponeException {
        new NonStrictExpectations() {
            {
                client.to(anyString);
                result = new IOException();
            }
        };

        try {
            sender.transmit(mail, client);
            fail("Exception must have been thrown");
        } catch (SendException e) {
            assertTrue(e.errorStatus().shouldRetry());
        }

    }

    @Test(expected = RemoteMtaErrorResponseException.class)
    public void testSendSmtpErrorResponse() throws SMTPException, IOException,
            RecipientsWereRejectedException, SendException, PostponeException {
        new NonStrictExpectations() {
            {
                client.dataEnd();
                result = SMTP_EXCEPTION;
            }
        };

        sender.transmit(mail, client);
    }

    @Test
    public void testConnectionRegistryMaintenance() throws Exception {
        new Expectations() {
            {
                outgoingConnectionsRegistry.openConnection(IP1);
                outgoingConnectionsRegistry.releaseConnection(IP1);
            }
        };

        sender.transmit(mail, client);
    }

    @Test(expected = PostponeException.class)
    public void testPostponedConnection() throws Exception {
        new Expectations() {
            {
                outgoingConnectionsRegistry.openConnection(IP1);
                result = POSTPONE_EXCEPTION;
            }
        };

        sender.transmit(mail, client);
    }
}
