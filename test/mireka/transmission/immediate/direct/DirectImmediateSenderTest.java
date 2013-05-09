package mireka.transmission.immediate.direct;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.util.Arrays;

import mireka.ExampleMail;
import mireka.address.Domain;
import mireka.address.Recipient;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.SendException;
import mireka.smtp.client.ClientFactory;
import mireka.smtp.client.MtaAddress;
import mireka.smtp.client.SmtpClient;
import mireka.transmission.Mail;
import mireka.transmission.immediate.DirectImmediateSender;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.dns.AddressLookup;
import mireka.transmission.immediate.dns.MxLookup;
import mireka.transmission.immediate.host.MailToHostTransmitter;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Name;

public class DirectImmediateSenderTest {

    @Tested
    private DirectImmediateSender sender;
    
    /** Automatically created by constructor, cannot be @Injected **/
    @Mocked
    private MxLookup mxLookup;

    /** Automatically created by constructor, cannot be @Injected **/
    @Mocked
    private AddressLookup addressLookup;

    @Injectable
    private ClientFactory clientFactory;
    
    @Mocked
    private SmtpClient client;
    
    @Injectable
    private MailToHostTransmitter mailToHostTransmitter;
    

    private final Mail mail = ExampleMail.simple();
    private Mail adaAddressLiteralMail;
    private Mail janeJoeMail;

    private final SendException permanentSendException = new SendException(
            "Example permanent failure",
            EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE);
    private final SendException transientSendException = new SendException(
            "Example transient failure",
            EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
    private static final PostponeException POSTPONE_EXCEPTION =
            new PostponeException(
                    10,
                    EnhancedStatus.TRANSIENT_SYSTEM_NOT_ACCEPTING_NETWORK_MESSAGES,
                    "Test exception");

    @Before
    public void initialize() {
        adaAddressLiteralMail = ExampleMail.simple();
        adaAddressLiteralMail.recipients =
                Arrays.asList(ADA_ADDRESS_LITERAL_AS_RECIPIENT);
        janeJoeMail = ExampleMail.simple();
        janeJoeMail.recipients =
                Arrays.asList(JANE_AS_RECIPIENT, JOHN_AS_RECIPIENT);
        
        new NonStrictExpectations() {
            {
                clientFactory.create();
                result = client;
            }
        };
    }

    @Test
    public void testSendToAddressLiteralVerifyNoDns() throws SendException,
            RecipientsWereRejectedException, PostponeException {

        sender.send(adaAddressLiteralMail);
        
        new Verifications() {
            {
                mxLookup.queryMxTargets((Domain)any);
                times = 0;

                addressLookup.queryAddresses((Name)any);
                times = 0;

                mailToHostTransmitter.transmit((Mail) any, null);
                
                client.setMtaAddress(new MtaAddress(ADDRESS_LITERAL, IP));
            }
        };
    }

    @Test
    public void testSendToDomain() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets((Domain)any);
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses((Name)any);
                result = new InetAddress[] { IP_ADDRESS_ONLY };

                client.setMtaAddress(new MtaAddress("host1.example.com", IP));
                
                mailToHostTransmitter.transmit((Mail) any, null);
            }
        };

        sender.send(mail);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendToDifferentDomain() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        mail.recipients =
                Arrays.asList(JANE_AS_RECIPIENT, NANCY_NET_AS_RECIPIENT);
        sender.send(mail);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendToGlobalPostmaster() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        mail.recipients =
                Arrays.asList((Recipient) GLOBAL_POSTMASTER_AS_RECIPIENT);
        sender.send(mail);
    }

    @Test
    public void testSendFirstMxCannotBeResolved() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets((Domain)any);
                result =
                        new Name[] { HOST1_EXAMPLE_COM_NAME,
                                HOST2_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses((Name)any);
                result = permanentSendException;
                result = new InetAddress[] { IP2 };

                client.setMtaAddress(new MtaAddress("host2.example.com", IP2));
                
                mailToHostTransmitter.transmit((Mail) any, null);
            }
        };

        sender.send(mail);
    }

    @Test
    public void testSendFirstHostHasTransientProblem() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        twoMxDnsExpectation();

        new Expectations() {
            {
                mailToHostTransmitter.transmit((Mail) any, null);
                result = transientSendException;

                mailToHostTransmitter.transmit((Mail) any, null);
                result = null;
            }
        };

        sender.send(mail);

    }

    private void twoMxDnsExpectation() throws SendException {
        new NonStrictExpectations() {
            {
                mxLookup.queryMxTargets((Domain)any);
                result =
                        new Name[] { HOST1_EXAMPLE_COM_NAME,
                                HOST2_EXAMPLE_COM_NAME };
                times = 1;

                addressLookup.queryAddresses((Name)any);
                result = new InetAddress[] { IP1 };
                result = new InetAddress[] { IP2 };
                times = 2;
            }
        };
    }

    @Test(expected = SendException.class)
    public void testSendFirstHostHasPermanentProblem() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets((Domain)any);
                result =
                        new Name[] { HOST1_EXAMPLE_COM_NAME,
                                HOST2_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses((Name)any);
                result = new InetAddress[] { IP1 };

                mailToHostTransmitter.transmit((Mail) any, null);
                result = permanentSendException;
            }
        };

        sender.send(mail);
    }

    @Test
    public void testSendFirstHostHasTransientSecondHasPermanentProblem()
            throws SendException, RecipientsWereRejectedException,
            PostponeException {
        twoMxDnsExpectation();
        new Expectations() {
            {
                mailToHostTransmitter.transmit((Mail) any, null);
                result = transientSendException;

                mailToHostTransmitter.transmit((Mail) any, null);
                result = permanentSendException;
            }
        };

        try {
            sender.send(mail);
            fail("Exception expected");
        } catch (SendException e) {
            assertFalse(e.errorStatus().shouldRetry());
        }

    }

    @Test
    public void testSendFirstHostPostponed() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        twoMxDnsExpectation();
        new Expectations() {
            {
                mailToHostTransmitter.transmit((Mail) any, null);
                result = POSTPONE_EXCEPTION;

                mailToHostTransmitter.transmit((Mail) any, null);
                result = null;
            }
        };

        sender.send(mail);

    }

    @Test
    public void testSendFirstHostPostponedSecondHasTransientProblem()
            throws SendException, RecipientsWereRejectedException,
            PostponeException {
        twoMxDnsExpectation();
        new Expectations() {
            {
                mailToHostTransmitter.transmit((Mail) any, null);
                result = POSTPONE_EXCEPTION;

                mailToHostTransmitter.transmit((Mail) any, null);
                result = transientSendException;
            }
        };

        try {
            sender.send(mail);
            fail("Exception expected");
        } catch (SendException e) {
            assertTrue(e.errorStatus().shouldRetry());
        }
    }

    @Test(expected = PostponeException.class)
    public void testSendBothPostponed() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        twoMxDnsExpectation();
        new Expectations() {
            {
                mailToHostTransmitter.transmit((Mail) any, null);
                result = POSTPONE_EXCEPTION;

                mailToHostTransmitter.transmit((Mail) any, null);
                result = POSTPONE_EXCEPTION;
            }
        };

        sender.send(mail);
    }

    @Test
    public void testSendSingleHostPermanentlyCannotBeResolved()
            throws SendException, RecipientsWereRejectedException,
            PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets((Domain)any);
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses((Name)any);
                result = permanentSendException;
            }
        };

        try {
            sender.send(mail);
            fail("An exception must have been thrown");
        } catch (SendException e) {
            assertFalse(e.errorStatus().shouldRetry());
        }
    }

    @Test
    public void testSendSingleHostTemporarilyCannotBeResolved()
            throws SendException, RecipientsWereRejectedException,
            PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets((Domain)any);
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses((Name)any);
                result = transientSendException;
            }
        };

        try {
            sender.send(mail);
            fail("An exception must have been thrown");
        } catch (SendException e) {
            assertTrue(e.errorStatus().shouldRetry());
        }
    }

    @Test(expected = PostponeException.class)
    public void testSendSingleHostPostponeException() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets((Domain)any);
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses((Name)any);
                result = new InetAddress[] { IP1 };

                mailToHostTransmitter.transmit((Mail) any, null);
                result = POSTPONE_EXCEPTION;
            }
        };

        sender.send(mail);
    }
}
