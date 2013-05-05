package mireka.transmission.immediate;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import mireka.ExampleMail;
import mireka.smtp.ClientFactory;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.Mail;
import mireka.transmission.immediate.host.MailToHostTransmitter;
import mireka.transmission.immediate.host.OutgoingConnectionsRegistry;
import mireka.transmission.queuing.LogIdFactory;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

public class MailToHostTransmitterTest {
    private static final PostponeException POSTPONE_EXCEPTION =
            new PostponeException(
                    10,
                    EnhancedStatus.TRANSIENT_SYSTEM_NOT_ACCEPTING_NETWORK_MESSAGES,
                    "Test exception");

    private static final SMTPException SMTP_EXCEPTION = new SMTPException(
            new SMTPClient.Response(500, "Test error"));

    @Mocked
    private ClientFactory clientFactory;

    @Mocked
    private SmartClient smartClient;

    @Mocked
    private LogIdFactory logIdFactory;

    @Mocked
    private OutgoingConnectionsRegistry outgoingConnectionsRegistry;

    private Mail mail = ExampleMail.simple();

    private RemoteMta remoteMta = new RemoteMta(HOST1_EXAMPLE_COM,
            IP1.getHostAddress());

    private MailToHostTransmitter sender;

    @Before
    public void initialize() throws UnknownHostException, SMTPException,
            IOException {
        new NonStrictExpectations() {
            {
                clientFactory.create((InetAddress) any);
                result = smartClient;
            }
        };
        sender =
                new MailToHostTransmitter(clientFactory,
                        outgoingConnectionsRegistry, logIdFactory, remoteMta);
    }

    @Test
    public void testSend() throws SendException,
            RecipientsWereRejectedException, IOException, PostponeException {

        sender.transmit(mail, IP1);

        new Verifications() {
            {
                smartClient.from(anyString);
                smartClient.to(anyString);
                smartClient.dataEnd();
                smartClient.quit();
            }
        };

    }

    @Test
    public void testSendAllAddressRejected() throws IllegalArgumentException,
            SendException, SMTPException, IOException, PostponeException {
        new NonStrictExpectations() {
            {
                smartClient.to(anyString);
                result = SMTP_EXCEPTION;

            }
        };

        try {
            sender.transmit(mail, IP1);
            fail("Exception must have been thrown");
        } catch (RecipientsWereRejectedException e) {
            assertEquals(e.rejections.size(), 1);
            assertEquals(e.rejections.get(0).recipient, JANE_AS_RECIPIENT);
        }

        new Verifications() {
            {
                smartClient.dataStart();
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
                smartClient.to(anyString);
                result = SMTP_EXCEPTION;
                result = null;
            }
        };

        try {
            mail.recipients =
                    Arrays.asList(JANE_AS_RECIPIENT, JOHN_AS_RECIPIENT);
            sender.transmit(mail, IP1);
            fail("Exception must have been thrown");
        } catch (RecipientsWereRejectedException e) {
            assertEquals(1, e.rejections.size());
            assertEquals(JANE_AS_RECIPIENT, e.rejections.get(0).recipient);
        }

        new Verifications() {
            {
                smartClient.dataStart();
            }
        };
    }

    @Test
    public void testSendIoExceptionIsTransient() throws SMTPException,
            IOException, RecipientsWereRejectedException, PostponeException {
        new NonStrictExpectations() {
            {
                smartClient.to(anyString);
                result = new IOException();
            }
        };

        try {
            sender.transmit(mail, IP1);
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
                smartClient.dataEnd();
                result = SMTP_EXCEPTION;
            }
        };

        sender.transmit(mail, IP1);
    }

    @Test
    public void testConnectionRegistryMaintenance() throws Exception {
        new Expectations() {
            {
                outgoingConnectionsRegistry.openConnection(IP1);
                outgoingConnectionsRegistry.releaseConnection(IP1);
            }
        };

        sender.transmit(mail, IP1);
    }

    @Test(expected = PostponeException.class)
    public void testPostponedConnection() throws Exception {
        new Expectations() {
            {
                outgoingConnectionsRegistry.openConnection(IP1);
                result = POSTPONE_EXCEPTION;
            }
        };

        sender.transmit(mail, IP1);
    }
}
