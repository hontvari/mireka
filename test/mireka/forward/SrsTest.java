package mireka.forward;

import static org.junit.Assert.*;

import java.security.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import mireka.filter.local.table.InlineDomainRegistry;
import mireka.smtp.address.DomainPart;
import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.RemotePartContainingRecipient;
import mireka.smtp.address.ReversePath;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTimeUtils;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SrsTest {
    final InlineDomainRegistry localDomains = new InlineDomainRegistry();

    public SrsTest() {
        localDomains.addDomain("example.com");
        localDomains.addDomain("example.net");
    }

    @Before
    public void setup() {
        DateTimeUtils.setCurrentMillisFixed(new Instant("2011-07-21T12:00Z")
                .getMillis());
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testTimestamp() {
        Srs srs = new Srs();
        srs.setMaximumAge(10);

        assertTrue(srs.isValidTimeslot(0, 0));
        assertTrue(srs.isValidTimeslot(0, 10));
        assertFalse(srs.isValidTimeslot(0, 11));
        // one day in the future is acceptable
        assertTrue(srs.isValidTimeslot(10, 9));
        // two days in the future are not
        assertFalse(srs.isValidTimeslot(10, 8));

        // wraparounds
        // one day in the future
        assertTrue(srs.isValidTimeslot(0, 1023));
        // two days in the future
        assertFalse(srs.isValidTimeslot(0, 1022));
        // timestamp is at the end of the window
        assertTrue(srs.isValidTimeslot(1020, 0));

    }

    @Test
    public final void testNormalReversePath() {
        Srs srs = new Srs();
        srs.setDefaultRemotePart(new DomainPart("example.net"));
        srs.setLocalDomains(localDomains);
        srs.setSecretKey("19AF");

        ReversePath originalReversePath =
                new MailAddressFactory()
                        .createReversePathAlreadyVerified("john@third-party.example.org");
        Recipient originalRecipient =
                new MailAddressFactory()
                        .createRecipientAlreadyVerified("jane@example.com");

        ReversePath newReversePath =
                srs.forward(originalReversePath, originalRecipient);

        assertEquals("SRS0=NKFJ=2I=third-party.example.org=john@example.com",
                newReversePath.getSmtpText());
    }

    @Test
    public final void testSrs0ReversePath() {
        Srs srs = new Srs();
        srs.setDefaultRemotePart(new DomainPart("we.example.net"));
        srs.setLocalDomains(localDomains);
        srs.setSecretKey("19AF");

        ReversePath originalReversePath =
                new MailAddressFactory()
                        .createReversePathAlreadyVerified("SRS0=uwWh=2I=source.example.com=john@forwarder.example.com");
        Recipient originalRecipient =
                new MailAddressFactory()
                        .createRecipientAlreadyVerified("jane@we.example.net");

        ReversePath newReversePath =
                srs.forward(originalReversePath, originalRecipient);

        assertEquals(
                "SRS1=jdhx=forwarder.example.com==uwWh=2I=source.example.com=john@we.example.net",
                newReversePath.getSmtpText());
    }

    @Test
    public final void testSrs1ReversePath() {
        Srs srs = new Srs();
        srs.setDefaultRemotePart(new DomainPart("we.example.net"));
        srs.setLocalDomains(localDomains);
        srs.setSecretKey("19AF");

        ReversePath originalReversePath =
                new MailAddressFactory()
                        .createReversePathAlreadyVerified("SRS1=AAAA=forwarder.example.com==uwWh=2I=source.example.com=john@bouncer.example.net");
        Recipient originalRecipient =
                new MailAddressFactory()
                        .createRecipientAlreadyVerified("jane@we.example.net");

        ReversePath newReversePath =
                srs.forward(originalReversePath, originalRecipient);

        assertEquals(
                "SRS1=jdhx=forwarder.example.com==uwWh=2I=source.example.com=john@we.example.net",
                newReversePath.getSmtpText());
    }

    @Test
    public void testPerlHashCompatibility() throws Exception {
        String source = "A";
        byte[] secretKey = "secret".getBytes("UTF-8");
        Mac mac = Mac.getInstance("HmacSHA1");
        Key key = new SecretKeySpec(secretKey, "HmacSHA1");
        mac.init(key);
        byte[] digestBytes = mac.doFinal(source.getBytes("UTF-8"));
        String hexDigest = Hex.encodeHexString(digestBytes);
        assertEquals("955a367a4c01f58118021054729c7fb54b5de94e", hexDigest);
    }

    @Test
    public final void testPerlCompatibility() throws InvalidSrsException {
        // Test values are coming from the SRS-0.31 PERL module on
        // the date which is included in the setup function.

        Srs srs = new Srs();
        srs.setDefaultRemotePart(new DomainPart("hostb.com"));
        srs.setLocalDomains(localDomains);
        srs.setSecretKeyString("secret");

        String rp0 = forward(srs, "usera@hosta.com", "userb@hostb.com");
        assertEquals("SRS0=VtG6=2I=hosta.com=usera@hostb.com", rp0);
        assertEquals("usera@hosta.com", reverse(srs, rp0));

        srs.setDefaultRemotePart(new DomainPart("hostc.com"));
        String rp1 = forward(srs, rp0, "userc@hostc.com");
        assertEquals("SRS1=IC2k=hostb.com==VtG6=2I=hosta.com=usera@hostc.com",
                rp1);
        assertEquals(rp0, reverse(srs, rp1));

        srs.setDefaultRemotePart(new DomainPart("hostd.com"));
        String rp2 = forward(srs, rp1, "userd@hostd.com");
        assertEquals("SRS1=IC2k=hostb.com==VtG6=2I=hosta.com=usera@hostd.com",
                rp2);
        assertEquals(rp0, reverse(srs, rp2));
    }

    private String forward(Srs srs, String originalReversePath,
            String originalRecipient) {
        ReversePath originalReversePathObject =
                new MailAddressFactory()
                        .createReversePathAlreadyVerified(originalReversePath);
        Recipient originalRecipientObject =
                new MailAddressFactory()
                        .createRecipientAlreadyVerified(originalRecipient);

        ReversePath newReversePath =
                srs.forward(originalReversePathObject, originalRecipientObject);
        return newReversePath.getSmtpText();
    }

    private String reverse(Srs srs, String srsRecipient)
            throws InvalidSrsException {
        Recipient recipient =
                new MailAddressFactory()
                        .createRecipientAlreadyVerified(srsRecipient);
        Recipient newRecipient = srs.reverse(recipient);
        return ((RemotePartContainingRecipient) newRecipient).getMailbox()
                .getSmtpText();
    }
}
