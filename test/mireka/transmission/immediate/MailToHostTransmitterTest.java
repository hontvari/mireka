package mireka.transmission.immediate;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import mireka.ClientFactory;
import mireka.ExampleMail;
import mireka.transmission.Mail;
import mireka.transmission.queuing.LogIdFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.subethamail.smtp.client.SMTPClient;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

@RunWith(MockitoJUnitRunner.class)
public class MailToHostTransmitterTest {
    @Mock
    private ClientFactory clientFactory;

    @Mock
    private SmartClient smartClient;

    @Mock
    private LogIdFactory logIdFactory;

    private Mail mail = ExampleMail.simple();

    private RemoteMta remoteMta =
            new RemoteMta(HOST1_EXAMPLE_COM, IP1.getHostAddress());

    private SMTPException smtpException =
            new SMTPException(new SMTPClient.Response(500, "Internal error"));

    private MailToHostTransmitter sender;

    @Before
    public void initialize() throws UnknownHostException, SMTPException,
            IOException {
        sender =
                new MailToHostTransmitter(clientFactory, logIdFactory,
                        remoteMta);
        when(clientFactory.create(any(InetAddress.class))).thenReturn(
                smartClient);
    }

    @Test
    public void testSend() throws SendException,
            RecipientsWereRejectedException, IOException {
        sender.transmit(mail, IP1);

        verify(smartClient).from(anyString());
        verify(smartClient).dataEnd();
        verify(smartClient).quit();
    }

    @SuppressWarnings("null")
    @Test
    public void testSendAllAddressRejected() throws IllegalArgumentException,
            SendException, SMTPException, IOException {
        doThrow(smtpException).when(smartClient).to(anyString());
        RecipientsWereRejectedException e = null;

        try {
            sender.transmit(mail, IP1);
            fail("Exception must have been thrown");
        } catch (RecipientsWereRejectedException e1) {
            e = e1;
        }

        assertEquals(e.rejections.size(), 1);
        assertEquals(e.rejections.get(0).recipient, JANE_AS_RECIPIENT);
        verify(smartClient, never()).dataStart();
    }

    @SuppressWarnings("null")
    @Test
    public void testSendFirstAddressRejectedFromTwo()
            throws IllegalArgumentException, SendException, SMTPException,
            IOException {
        mail.recipients = Arrays.asList(JANE_AS_RECIPIENT, JOHN_AS_RECIPIENT);
        doThrow(smtpException).doNothing().when(smartClient).to(anyString());
        RecipientsWereRejectedException e = null;

        try {
            sender.transmit(mail, IP1);
            fail("Exception must have been thrown");
        } catch (RecipientsWereRejectedException e1) {
            e = e1;
        }

        assertEquals(e.rejections.size(), 1);
        assertEquals(e.rejections.get(0).recipient, JANE_AS_RECIPIENT);
        verify(smartClient).dataStart();
    }

    @SuppressWarnings("null")
    @Test
    public void testSendIoExceptionIsTransient() throws SMTPException,
            IOException, RecipientsWereRejectedException {
        doThrow(new IOException()).when(smartClient).to(anyString());
        SendException e = null;

        try {
            sender.transmit(mail, IP1);
            fail("Exception must have been thrown");
        } catch (SendException e1) {
            e = e1;
        }

        assertTrue(e.errorStatus().shouldRetry());
    }

    @Test(expected = RemoteMtaErrorResponseException.class)
    public void testSendSmtpErrorResponse() throws SMTPException, IOException,
            RecipientsWereRejectedException, SendException {
        doThrow(smtpException).when(smartClient).dataEnd();

        sender.transmit(mail, IP1);
    }
}
