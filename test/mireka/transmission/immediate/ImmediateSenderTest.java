package mireka.transmission.immediate;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.util.Arrays;

import mireka.ExampleMail;
import mireka.address.Domain;
import mireka.address.Recipient;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.Mail;
import mireka.transmission.immediate.dns.AddressLookup;
import mireka.transmission.immediate.dns.AddressLookupFactory;
import mireka.transmission.immediate.dns.MxLookup;
import mireka.transmission.immediate.dns.MxLookupFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xbill.DNS.Name;

@RunWith(MockitoJUnitRunner.class)
public class ImmediateSenderTest {

    @Mock
    private MxLookupFactory mxLookupFactory;

    @Mock
    private MxLookup mxLookup;

    @Mock
    private AddressLookupFactory addressLookupFactory;

    @Mock
    private AddressLookup addressLookup;

    @Mock
    private MailToHostTransmitterFactory mailToHostTransmitterFactory;

    @Mock
    private MailToHostTransmitter mailToHostTransmitter;

    private Mail mail = ExampleMail.simple();
    private Mail adaAddressLiteralMail;
    private Mail janeJoeMail;

    private ImmediateSender sender;

    private SendException permanentSendException =
            new SendException("Example permanent failure",
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE);
    private SendException transientSendException =
            new SendException("Example transient failure",
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);

    @Before
    public void initialize() {
        adaAddressLiteralMail = ExampleMail.simple();
        adaAddressLiteralMail.recipients =
                Arrays.asList(ADA_ADDRESS_LITERAL_AS_RECIPIENT);
        janeJoeMail = ExampleMail.simple();
        janeJoeMail.recipients =
                Arrays.asList(JANE_AS_RECIPIENT, JOHN_AS_RECIPIENT);
        sender =
                new ImmediateSender(mxLookupFactory, addressLookupFactory,
                        mailToHostTransmitterFactory);
        when(mailToHostTransmitterFactory.create(any(RemoteMta.class)))
                .thenReturn(mailToHostTransmitter);
        when(mxLookupFactory.create(any(Domain.class))).thenReturn(mxLookup);
        when(addressLookupFactory.create(any(Name.class))).thenReturn(
                addressLookup);
    }

    @Test
    public void testSendToAddressLiteralVerifyNoDns() throws SendException,
            RecipientsWereRejectedException {
        sender.send(adaAddressLiteralMail);

        verify(mailToHostTransmitter).transmit(any(Mail.class),
                eq(IP_ADDRESS_ONLY));
        verifyZeroInteractions(addressLookup, mxLookup);
    }

    @Test
    public void testSendToDomain() throws SendException,
            RecipientsWereRejectedException {
        when(mxLookup.queryMxTargets()).thenReturn(
                new Name[] { HOST1_EXAMPLE_COM_NAME });
        when(addressLookup.queryAddresses()).thenReturn(
                new InetAddress[] { IP_ADDRESS_ONLY });

        sender.send(mail);

        verify(mailToHostTransmitter).transmit(any(Mail.class),
                eq(IP_ADDRESS_ONLY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendToDifferentDomain() throws IllegalArgumentException,
            SendException, RecipientsWereRejectedException {
        mail.recipients =
                Arrays.asList(JANE_AS_RECIPIENT, NANCY_NET_AS_RECIPIENT);
        sender.send(mail);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendToGlobalPostmaster() throws IllegalArgumentException,
            SendException, RecipientsWereRejectedException {
        mail.recipients =
                Arrays.asList((Recipient) GLOBAL_POSTMASTER_AS_RECIPIENT);
        sender.send(mail);
    }

    @Test
    public void testSendFirstMxCannotBeResolved()
            throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException {
        when(mxLookup.queryMxTargets()).thenReturn(
                new Name[] { HOST1_EXAMPLE_COM_NAME, HOST2_EXAMPLE_COM_NAME });
        when(addressLookup.queryAddresses()).thenThrow(permanentSendException)
                .thenReturn(new InetAddress[] { IP2 });

        sender.send(mail);

        verify(addressLookup, times(2)).queryAddresses();
        verify(mailToHostTransmitter).transmit(any(Mail.class), eq(IP2));
    }

    @Test
    public void testSendFirstHostHasTransientProblem()
            throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException {
        when(mxLookup.queryMxTargets()).thenReturn(
                new Name[] { HOST1_EXAMPLE_COM_NAME, HOST2_EXAMPLE_COM_NAME });
        when(addressLookup.queryAddresses()).thenReturn(
                new InetAddress[] { IP1 });
        doThrow(transientSendException).doNothing().when(mailToHostTransmitter)
                .transmit(any(Mail.class), any(InetAddress.class));

        sender.send(mail);

        verify(addressLookup, times(2)).queryAddresses();
        verify(mailToHostTransmitter, times(2)).transmit(any(Mail.class),
                any(InetAddress.class));
    }

    @Test
    public void testSendFirstHostHasPermanentProblem()
            throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException {
        when(mxLookup.queryMxTargets()).thenReturn(
                new Name[] { HOST1_EXAMPLE_COM_NAME, HOST2_EXAMPLE_COM_NAME });
        when(addressLookup.queryAddresses()).thenReturn(
                new InetAddress[] { IP1 });
        doThrow(permanentSendException).doNothing().when(mailToHostTransmitter)
                .transmit(any(Mail.class), any(InetAddress.class));

        try {
            sender.send(mail);
            fail("An exception must have been thrown");
        } catch (SendException e) {
            verify(mailToHostTransmitter, times(1)).transmit(any(Mail.class),
                    any(InetAddress.class));
        }
    }

    @Test
    public void testSendSingleHostPermanentlyCannotBeResolved()
            throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException {
        when(mxLookup.queryMxTargets()).thenReturn(
                new Name[] { HOST1_EXAMPLE_COM_NAME });
        when(addressLookup.queryAddresses()).thenThrow(permanentSendException);

        try {
            sender.send(mail);
            fail("An exception must have been thrown");
        } catch (SendException e) {
            assertFalse(e.errorStatus().shouldRetry());
        }
    }

    @Test
    public void testSendSingleHostTemporarilyCannotBeResolved()
            throws IllegalArgumentException, SendException,
            RecipientsWereRejectedException {
        when(mxLookup.queryMxTargets()).thenReturn(
                new Name[] { HOST1_EXAMPLE_COM_NAME });
        when(addressLookup.queryAddresses()).thenThrow(transientSendException);

        try {
            sender.send(mail);
            fail("An exception must have been thrown");
        } catch (SendException e) {
            assertTrue(e.errorStatus().shouldRetry());
        }
    }
}
