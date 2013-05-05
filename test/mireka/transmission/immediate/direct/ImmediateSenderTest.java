package mireka.transmission.immediate.direct;

import static mireka.ExampleAddress.ADA_ADDRESS_LITERAL_AS_RECIPIENT;
import static mireka.ExampleAddress.GLOBAL_POSTMASTER_AS_RECIPIENT;
import static mireka.ExampleAddress.HOST1_EXAMPLE_COM_NAME;
import static mireka.ExampleAddress.HOST2_EXAMPLE_COM_NAME;
import static mireka.ExampleAddress.IP;
import static mireka.ExampleAddress.IP1;
import static mireka.ExampleAddress.IP2;
import static mireka.ExampleAddress.IP_ADDRESS_ONLY;
import static mireka.ExampleAddress.JANE_AS_RECIPIENT;
import static mireka.ExampleAddress.JOHN_AS_RECIPIENT;
import static mireka.ExampleAddress.NANCY_NET_AS_RECIPIENT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.util.Arrays;

import mireka.ExampleMail;
import mireka.address.Domain;
import mireka.address.Recipient;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.Mail;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.RemoteMta;
import mireka.transmission.immediate.SendException;
import mireka.transmission.immediate.dns.AddressLookup;
import mireka.transmission.immediate.dns.AddressLookupFactory;
import mireka.transmission.immediate.dns.MxLookup;
import mireka.transmission.immediate.dns.MxLookupFactory;
import mireka.transmission.immediate.host.MailToHostTransmitter;
import mireka.transmission.immediate.host.MailToHostTransmitterFactory;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Name;

public class ImmediateSenderTest {

    @Mocked
    private MxLookupFactory mxLookupFactory;

    @Mocked
    private MxLookup mxLookup;

    @Mocked
    private AddressLookupFactory addressLookupFactory;

    @Mocked
    private AddressLookup addressLookup;

    @Mocked
    private MailToHostTransmitterFactory mailToHostTransmitterFactory;

    @Mocked
    private MailToHostTransmitter mailToHostTransmitter;

    private Mail mail = ExampleMail.simple();
    private Mail adaAddressLiteralMail;
    private Mail janeJoeMail;

    private DirectImmediateSender sender;

    private SendException permanentSendException = new SendException(
            "Example permanent failure",
            EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE);
    private SendException transientSendException = new SendException(
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
        sender =
                new DirectImmediateSender(mxLookupFactory, addressLookupFactory,
                        mailToHostTransmitterFactory);

        new NonStrictExpectations() {
            {
                mailToHostTransmitterFactory.create((RemoteMta) any);
                result = mailToHostTransmitter;

                mxLookupFactory.create((Domain) any);
                result = mxLookup;

                addressLookupFactory.create((Name) any);
                result = addressLookup;
            }
        };
    }

    @Test
    public void testSendToAddressLiteralVerifyNoDns() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets();
                times = 0;

                addressLookup.queryAddresses();
                times = 0;

                mailToHostTransmitter.transmit((Mail) any, IP);
            }
        };

        sender.send(adaAddressLiteralMail);
    }

    @Test
    public void testSendToDomain() throws SendException,
            RecipientsWereRejectedException, PostponeException {
        new Expectations() {
            {
                mxLookup.queryMxTargets();
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses();
                result = new InetAddress[] { IP_ADDRESS_ONLY };

                mailToHostTransmitter.transmit((Mail) any, IP_ADDRESS_ONLY);
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
                mxLookup.queryMxTargets();
                result =
                        new Name[] { HOST1_EXAMPLE_COM_NAME,
                                HOST2_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses();
                result = permanentSendException;
                result = new InetAddress[] { IP2 };

                mailToHostTransmitter.transmit((Mail) any, IP2);
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
                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
                result = transientSendException;

                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
                result = null;
            }
        };

        sender.send(mail);

    }

    private void twoMxDnsExpectation() throws SendException {
        new NonStrictExpectations() {
            {
                mxLookup.queryMxTargets();
                result =
                        new Name[] { HOST1_EXAMPLE_COM_NAME,
                                HOST2_EXAMPLE_COM_NAME };
                times = 1;

                addressLookup.queryAddresses();
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
                mxLookup.queryMxTargets();
                result =
                        new Name[] { HOST1_EXAMPLE_COM_NAME,
                                HOST2_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses();
                result = new InetAddress[] { IP1 };

                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
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
                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
                result = transientSendException;

                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
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
                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
                result = POSTPONE_EXCEPTION;

                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
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
                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
                result = POSTPONE_EXCEPTION;

                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
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
                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
                result = POSTPONE_EXCEPTION;

                mailToHostTransmitter.transmit((Mail) any, (InetAddress) any);
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
                mxLookup.queryMxTargets();
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses();
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
                mxLookup.queryMxTargets();
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses();
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
                mxLookup.queryMxTargets();
                result = new Name[] { HOST1_EXAMPLE_COM_NAME };

                addressLookup.queryAddresses();
                result = new InetAddress[] { IP1 };

                mailToHostTransmitter.transmit((Mail) any, IP1);
                result = POSTPONE_EXCEPTION;
            }
        };

        sender.send(mail);
    }
}
