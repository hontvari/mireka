package mireka.forward;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import mireka.ConfigurationException;
import mireka.filter.local.table.RemotePartSpecification;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Mailbox;
import mireka.smtp.address.RealReversePath;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.RemotePart;
import mireka.smtp.address.RemotePartContainingRecipient;
import mireka.smtp.address.ReversePath;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Srs class implements the Sender Rewriting Scheme (SRS), which makes
 * possible to forward mail without breaking SPF checks.
 * <p>
 * Note: sender rewriting is not necessary if the reverse path is local.
 * 
 * @see <a href="http://www.openspf.org/SRS">SRS</a>
 */
public class Srs {
    /**
     * Count of possible timestamp values.
     */
    private static final int TIMESLOTS = 32 * 32;
    /**
     * Duration of a single timestamp value, in milliseconds.
     */
    private static final long PRECISION = 1000 * 24 * 60 * 60;
    public static final Pattern SRS0_PREFIX = Pattern.compile("SRS0[=+-]");
    public static final Pattern SRS1_PREFIX = Pattern.compile("SRS1[=+-]");
    /**
     * Domains which authorizes this server to send mail in their name using the
     * SPF DNS record. If not set, than it is assumed that this server is
     * authorized to send mails in the name of all domains for which it accepts
     * mail.
     */
    private RemotePartSpecification localDomains;
    private RemotePart defaultRemotePart;
    private byte[] secretKey;
    /**
     * Validity of the timestamp in days.
     */
    private int maximumAge = 21;

    /**
     * Returns a reverse path which can be used in the forwarded mail.
     * 
     * @param reversePath
     *            the reversePath which with our server received the mail to be
     *            forwarded
     * @param originalRecipient
     *            the mail was received for this recipient, which recipient
     *            address is configured to forward mail to another address.
     */
    public ReversePath forward(ReversePath reversePath,
            Recipient originalRecipient) {
        return new ForwardRewriter(reversePath, originalRecipient)
                .rewriteSender();
    }

    public Recipient reverse(Recipient srsRecipient) throws InvalidSrsException {
        return new ReverseRewriter(srsRecipient).rewriteRecipient();
    }

    private String calculateHash(String source) {
        try {
            if (secretKey == null)
                throw new ConfigurationException(
                        "SRS Secret key is not configured.");
            byte[] sourceBytes =
                    source.toLowerCase(Locale.US).getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA1");
            Key key = new SecretKeySpec(secretKey, "HmacSHA1");
            mac.init(key);
            byte[] digestBytes = mac.doFinal(sourceBytes);
            String digestBase64 = Base64.encodeBase64String(digestBytes);
            return digestBase64.substring(0, 4);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * Throws an exception if the timestamp in an SRS0 reverse path is expired.
     * 
     * @param timestamp
     *            the timestamp in an SRS reverse path, converted to a timeslot
     *            value, which is an integer from 0 inclusive to TIMESLOTS
     *            exclusive.
     * @throws InvalidSrsException
     *             if the timestamp is too old or too far in the future. It is
     *             allowed to be in the future with 1 day, in order to prevent a
     *             small clock change in the wrong moment to cause rejections.
     */
    boolean isValidTimeslot(int timestamp, int today) {
        int firstValid = timestamp - 1;
        if (firstValid < 0)
            firstValid += TIMESLOTS;
        int lastValid = firstValid + 1 + getMaximumAge();
        if (today < firstValid)
            today += TIMESLOTS;
        return today <= lastValid;
    }

    private class ForwardRewriter {
        ReversePath originalReversePath;
        /**
         * The original recipient of the mail which will be forwarded.
         */
        Recipient originalRecipient;
        /**
         * The mailbox specified in the original reverse path.
         */
        Mailbox mailbox;

        ForwardRewriter(ReversePath reversePath, Recipient originalRecipient) {
            this.originalReversePath = reversePath;
            this.originalRecipient = originalRecipient;
            if (reversePath instanceof RealReversePath)
                this.mailbox = ((RealReversePath) reversePath).getMailbox();
        }

        public ReversePath rewriteSender() {
            if (isRewriteRequired())
                return doRewrite();
            else
                return originalReversePath;
        }

        private boolean isRewriteRequired() {
            return !originalReversePath.isNull() && !isLocalReversePathDomain();
        }

        private boolean isLocalReversePathDomain() {
            if (localDomains != null) {
                return localDomains.isSatisfiedBy(mailbox.getRemotePart());
            } else {
                if (originalRecipient instanceof RemotePartContainingRecipient) {
                    RemotePart recipientRemotePart =
                            ((RemotePartContainingRecipient) originalRecipient)
                                    .getMailbox().getRemotePart();
                    return mailbox.getRemotePart().equals(recipientRemotePart);
                } else {
                    return mailbox.getRemotePart().equals(defaultRemotePart);
                }
            }
        }

        private ReversePath doRewrite() {
            String localPartSmtpText = mailbox.getLocalPart().smtpText();

            if (SRS0_PREFIX.matcher(localPartSmtpText).lookingAt()) {
                return rewriteSrs0();
            } else if (SRS1_PREFIX.matcher(localPartSmtpText).lookingAt()) {
                try {
                    return rewriteSrs1();
                } catch (InvalidSrsException e) {
                    // not a real SRS1 reverse path, maybe it is similar
                    // accidentally.
                    return rewriteNotSrs();
                }
            } else {
                return rewriteNotSrs();
            }

        }

        private ReversePath rewriteNotSrs() {
            String timestamp = calculateTimestamp();
            String host = mailbox.getRemotePart().smtpText();
            String localPart = mailbox.getLocalPart().smtpText();
            String hash = calculateHash(timestamp + host + localPart);
            RemotePart rewrittenRemotePart = calculateRewrittenRemotePart();

            StringBuilder buffer = new StringBuilder();
            buffer.append("SRS0=");
            buffer.append(hash).append('=');
            buffer.append(timestamp).append('=');
            buffer.append(host).append('=');
            buffer.append(localPart);
            buffer.append('@');
            buffer.append(rewrittenRemotePart.smtpText());

            return new MailAddressFactory()
                    .createReversePathAlreadyVerified(buffer.toString());
        }

        private String calculateTimestamp() {
            int daysSinceEpoch =
                    (int) (DateTimeUtils.currentTimeMillis() / 1000 / 24 / 60 / 60);
            int modulo1 = daysSinceEpoch % (2 << 10);
            int modulo = modulo1;
            return Base32Int.encode10Bits(modulo);
        }

        private RemotePart calculateRewrittenRemotePart() {
            if (originalRecipient.isGlobalPostmaster()) {
                if (defaultRemotePart == null)
                    throw new ConfigurationException(
                            "Mails sent to the global Postmaster are "
                                    + "forwarded, but "
                                    + "no default domain is specified "
                                    + "which can be used in SRS.");
                return defaultRemotePart;
            }

            RemotePartContainingRecipient remotePartContainingRecipient =
                    (RemotePartContainingRecipient) originalRecipient;
            RemotePart recipientRemotePart =
                    remotePartContainingRecipient.getMailbox().getRemotePart();
            if (localDomains == null
                    || localDomains.isSatisfiedBy(recipientRemotePart)) {
                return recipientRemotePart;
            } else {
                if (defaultRemotePart == null)
                    throw new ConfigurationException(
                            "The domain of a forwarded address does not "
                                    + "authorizes this server to send mail, "
                                    + "and no default domain is defined "
                                    + "which can be used in SRS.");
                return defaultRemotePart;
            }
        }

        private ReversePath rewriteSrs0() {
            String host = mailbox.getRemotePart().smtpText();
            String localPart = mailbox.getLocalPart().smtpText();
            // remove the SRS0 prefix
            localPart = localPart.substring(4);

            String hash = calculateHash(host + localPart);
            RemotePart rewrittenRemotePart = calculateRewrittenRemotePart();

            StringBuilder buffer = new StringBuilder();
            buffer.append("SRS1=");
            buffer.append(hash).append('=');
            buffer.append(host).append('=');
            buffer.append(localPart);
            buffer.append('@');
            buffer.append(rewrittenRemotePart.smtpText());

            return new MailAddressFactory()
                    .createReversePathAlreadyVerified(buffer.toString());
        }

        private ReversePath rewriteSrs1() throws InvalidSrsException {
            PersedSrs1LocalPart parsedSrs1 = PersedSrs1LocalPart.parse(mailbox);

            String hash =
                    calculateHash(parsedSrs1.originalHost
                            + parsedSrs1.compactOriginalLocalPart);
            RemotePart rewrittenRemotePart = calculateRewrittenRemotePart();

            StringBuilder buffer = new StringBuilder();
            buffer.append("SRS1=");
            buffer.append(hash).append('=');
            buffer.append(parsedSrs1.originalHost).append('=');
            buffer.append(parsedSrs1.compactOriginalLocalPart);
            buffer.append('@');
            buffer.append(rewrittenRemotePart.smtpText());

            return new MailAddressFactory()
                    .createReversePathAlreadyVerified(buffer.toString());
        }
    }

    private class ReverseRewriter {
        private final Logger logger = LoggerFactory
                .getLogger(ReverseRewriter.class);
        private Recipient originalRecipient;
        private Mailbox mailbox;

        ReverseRewriter(Recipient originalRecipient) {
            this.originalRecipient = originalRecipient;
            if (originalRecipient instanceof RemotePartContainingRecipient)
                mailbox =
                        ((RemotePartContainingRecipient) originalRecipient)
                                .getMailbox();
        }

        public Recipient rewriteRecipient() throws InvalidSrsException {
            if (originalRecipient.isGlobalPostmaster())
                return originalRecipient;

            String localPartSmtpText = mailbox.getLocalPart().smtpText();

            if (SRS0_PREFIX.matcher(localPartSmtpText).lookingAt()) {
                return rewriteSrs0();
            } else if (SRS1_PREFIX.matcher(localPartSmtpText).lookingAt()) {
                return rewriteSrs1();
            } else {
                throw new RuntimeException("Assertion failed");
            }
        }

        private Recipient rewriteSrs0() throws InvalidSrsException {
            PersedSrs0LocalPart parsed = PersedSrs0LocalPart.parse(mailbox);
            checkHash(parsed);
            checkTimestamp(parsed.timestamp);
            String recipientString =
                    parsed.originalLocalPart + '@' + parsed.originalHost;
            return new MailAddressFactory()
                    .createRecipientAlreadyVerified(recipientString);
        }

        private void checkHash(PersedSrs0LocalPart parsed)
                throws InvalidSrsException {
            String calculatedHash =
                    calculateHash(parsed.timestamp + parsed.originalHost
                            + parsed.originalLocalPart);
            checkHash(calculatedHash, parsed.hash);
        }

        /**
         * Compares expected and received digital signature both case
         * sensitively and case insensitively.
         */
        private void checkHash(String calculatedHash, String extractedHash)
                throws InvalidSrsException {
            if (calculatedHash.equals(extractedHash))
                return;
            if (calculatedHash.equalsIgnoreCase(extractedHash)) {
                logger.warn("Case insensitive hash match detected. Someone smashed case in the local-part. "
                        + mailbox.getSmtpText());
                return;
            }
            throw new InvalidSrsException("Hashes does not match. "
                    + mailbox.getSmtpText(), new EnhancedStatus(553, "5.1.0",
                    "SRS hash is invalid"));
        }

        private void checkTimestamp(String timestamp)
                throws InvalidSrsException {
            try {
                int timestampTimeslot = Base32Int.decode(timestamp);
                if (!isValidTimeslot(timestampTimeslot, todayTimeslot()))
                    throw new InvalidSrsException("Timestamp is too old. "
                            + mailbox.getSmtpText(), new EnhancedStatus(553,
                            "5.1.0", "SRS timestamp expired"));
            } catch (NumberFormatException e) {
                throw new InvalidSrsException("Invalid Base32 digit in "
                        + mailbox.getSmtpText(), new EnhancedStatus(553,
                        "5.1.0", "SRS address format invalid"));
            }

        }

        private int todayTimeslot() {
            return (int) ((DateTimeUtils.currentTimeMillis() / PRECISION) % TIMESLOTS);
        }

        private Recipient rewriteSrs1() throws InvalidSrsException {
            PersedSrs1LocalPart parsedLocalPart =
                    PersedSrs1LocalPart.parse(mailbox);
            checkHash(parsedLocalPart);
            String recipientString =
                    "SRS0" + parsedLocalPart.compactOriginalLocalPart + '@'
                            + parsedLocalPart.originalHost;
            return new MailAddressFactory()
                    .createRecipientAlreadyVerified(recipientString);
        }

        private void checkHash(PersedSrs1LocalPart parsed)
                throws InvalidSrsException {
            String calculatedHash =
                    calculateHash(parsed.originalHost
                            + parsed.compactOriginalLocalPart);
            checkHash(calculatedHash, parsed.hash);
        }
    }

    private static class PersedSrs1LocalPart {
        String hash;
        String originalHost;
        String compactOriginalLocalPart;

        static PersedSrs1LocalPart parse(Mailbox mailbox)
                throws InvalidSrsException {
            String localPart = mailbox.getLocalPart().smtpText();
            // remove the "SRS1=" prefix
            localPart = localPart.substring(5);
            String[] fields = localPart.split("=", 3);
            if (fields.length != 3)
                throw new InvalidSrsException("Less then three '=' separated "
                        + "fields after 'SRS1[=+-]' in "
                        + mailbox.getSmtpText(), new EnhancedStatus(553,
                        "5.1.0", "SRS address format invalid"));
            PersedSrs1LocalPart result = new PersedSrs1LocalPart();
            result.hash = fields[0];
            result.originalHost = fields[1];
            result.compactOriginalLocalPart = fields[2];
            return result;
        }
    }

    private static class PersedSrs0LocalPart {
        String hash;
        String timestamp;
        String originalHost;
        String originalLocalPart;

        static PersedSrs0LocalPart parse(Mailbox mailbox)
                throws InvalidSrsException {
            String localPart = mailbox.getLocalPart().smtpText();
            // remove the "SRS0=" prefix
            localPart = localPart.substring(5);
            String[] fields = localPart.split("=", 4);
            if (fields.length != 4)
                throw new InvalidSrsException("Less then four '=' separated "
                        + "fields after 'SRS0[=+-]' in "
                        + mailbox.getSmtpText(), new EnhancedStatus(553,
                        "5.1.0", "SRS address format invalid"));
            PersedSrs0LocalPart result = new PersedSrs0LocalPart();
            result.hash = fields[0];
            result.timestamp = fields[1];
            result.originalHost = fields[2];
            result.originalLocalPart = fields[3];
            return result;
        }
    }

    /**
     * @x.category GETSET
     */
    public RemotePartSpecification getLocalDomains() {
        return localDomains;
    }

    /**
     * Sets the domains which authorize this server to send mail in their name
     * using the SPF DNS record. Reverse paths from these domains will not be
     * rewritten. If not set, than it is assumed that the domain of any accepted
     * mail authorizes this server to send mail in its name.
     * 
     * @x.category GETSET
     */
    public void setLocalDomains(RemotePartSpecification localDomains) {
        this.localDomains = localDomains;
    }

    /**
     * @x.category GETSET
     */
    public RemotePart getDefaultRemotePart() {
        return defaultRemotePart;
    }

    /**
     * Sets the remote part used in the rewritten reverse path for those
     * recipients whose domain does not appear in the {@link #localDomains}
     * field, but for some reason mails to those domains are accepted. For
     * example mail sent to the global Postmaster address has no domain at all.
     * 
     * @x.category GETSET
     */
    public void setDefaultRemotePart(RemotePart defaultRemotePart) {
        this.defaultRemotePart = defaultRemotePart;
    }

    /**
     * @x.category GETSET
     */
    public void setDefaultRemotePart(String defaultRemotePart) {
        this.defaultRemotePart =
                new MailAddressFactory()
                        .createRemotePartFromDisplayableText(defaultRemotePart);
    }

    /**
     * @x.category GETSET
     */
    public String getSecretKey() {
        return Hex.encodeHexString(secretKey);
    }

    /**
     * Sets the secret key as a HEX string. It should be long enough to be hard
     * to recover with a brute force attack.
     * 
     * @x.category GETSET
     */
    public void setSecretKey(String secretKey) {
        try {
            this.secretKey = Hex.decodeHex(secretKey.toCharArray());
        } catch (DecoderException e) {
            throw new ConfigurationException(
                    "Invalid secret key: " + secretKey, e);
        }
    }

    /**
     * Sets the secret key by encoding the supplied String with UTF-8 to get the
     * key bytes.
     * 
     * @x.category GETSET
     */
    public void setSecretKeyString(String secretKey) {
        try {
            this.secretKey = secretKey.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * @x.category GETSET
     */
    public void setMaximumAge(int maximumAge) {
        this.maximumAge = maximumAge;
    }

    /**
     * @x.category GETSET
     */
    public int getMaximumAge() {
        return maximumAge;
    }

}
