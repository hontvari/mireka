package mireka.transmission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import mireka.transmission.immediate.ResponseParser;
import mireka.transmission.immediate.Rfc821Status;
import mireka.util.Multiline;

import org.subethamail.smtp.RejectException;

/**
 * These class represents an SMTP status which includes enhanced status code.
 * 
 * @see ResponseParser
 * @see <a href="http://tools.ietf.org/html/rfc3463">rfc3463 - Enhanced Mail
 *      System Status Codes</a>
 * @see <a href="http://tools.ietf.org/html/rfc2034">rfc2034 - SMTP Service
 *      Extension for Returning Enhanced Error Codes</a>
 */
public class EnhancedStatus implements MailSystemStatus {
    public static final EnhancedStatus TRANSIENT_SYSTEM_NOT_ACCEPTING_NETWORK_MESSAGES =
            new EnhancedStatus(421, "4.3.2",
                    "System not accepting network messages");
    public static final EnhancedStatus TRANSIENT_DIRECTORY_SERVER_FAILURE =
            new EnhancedStatus(450, "4.4.3", "Directory server failure");
    public static final EnhancedStatus BAD_DESTINATION_SYSTEM_ADDRESS =
            new EnhancedStatus(550, "5.1.2", "Bad destination system address");
    public static final EnhancedStatus PERMANENT_UNABLE_TO_ROUTE =
            new EnhancedStatus(550, "5.4.4", "Unable to route");
    public static final EnhancedStatus TRANSIENT_LOCAL_ERROR_IN_PROCESSING =
            new EnhancedStatus(451, "4.3.0", "Local error in processing");
    public static final EnhancedStatus MAIL_SYSTEM_FULL = new EnhancedStatus(
            452, "4.3.1", "Mail system full");
    public static final EnhancedStatus BAD_DESTINATION_MAILBOX_ADDRESS_SYNTAX =
            new EnhancedStatus(553, "5.1.3",
                    "Bad destination mailbox address syntax");
    public static final EnhancedStatus PERMANENT_INTERNAL_ERROR =
            new EnhancedStatus(554, "5.3.0", "Internal error");
    private final int smtpReplyCode;
    private final String enhancedStatusCode;
    private final String message;

    public EnhancedStatus(int smtpReplyCode, String enhancedStatusCode,
            String message) {
        this.smtpReplyCode = smtpReplyCode;
        this.enhancedStatusCode = enhancedStatusCode;
        this.message = message;
    }

    public EnhancedStatus(Rfc821Status response) {
        this.smtpReplyCode = response.getSmtpReplyCode();
        this.message = response.getMessage();
        this.enhancedStatusCode = approximateEnhancedCodeFromSmtpReplyCode();
    }

    /**
     * Returns an enhanced status code which roughly correspond to
     * {@link #smtpReplyCode}.
     */
    private String approximateEnhancedCodeFromSmtpReplyCode() {
        if (200 <= smtpReplyCode && smtpReplyCode <= 299)
            return "2.0.0";
        else if (400 <= smtpReplyCode && smtpReplyCode <= 499)
            return "4.0.0";
        else if (500 <= smtpReplyCode && smtpReplyCode <= 599)
            return "5.0.0";
        else
            throw new RuntimeException("Unexpected: "
                    + Integer.toString(smtpReplyCode));
    }

    public int getSmtpReplyCode() {
        return smtpReplyCode;
    }

    public String getEnhancedStatusCode() {
        return enhancedStatusCode;
    }

    public String getMessage() {
        return message;
    }

    /**
     * it returns true, if repeating the action may help
     */
    public boolean shouldRetry() {
        switch (getStatusClass()) {
        case TransientFailure:
            return true;
        case PermanentFailure:
            return false;
        default:
            throw new RuntimeException(getStatusClass().toString());
        }
    }

    private StatusClass getStatusClass() {
        if (smtpReplyCode >= 200 && smtpReplyCode <= 299)
            return StatusClass.Success;
        else if (smtpReplyCode >= 400 && smtpReplyCode <= 499)
            return StatusClass.TransientFailure;
        else if (smtpReplyCode >= 500 && smtpReplyCode <= 599)
            return StatusClass.PermanentFailure;
        else
            throw new RuntimeException("Unexpected: "
                    + Integer.toString(smtpReplyCode));
    }

    public String getMessagePrefixedWithEnhancedStatusCode() {
        try {
            if (message.isEmpty())
                return enhancedStatusCode;
            BufferedReader reader =
                    new BufferedReader(new StringReader(message));
            String line;
            StringBuilder buffer = new StringBuilder();
            boolean firstLine = true;
            while (null != (line = reader.readLine())) {
                if (!firstLine)
                    buffer.append("\r\n");
                firstLine = false;
                buffer.append(enhancedStatusCode);
                buffer.append(' ');
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            throw new RuntimeException(); // impossible
        }
    }

    @Override
    public String getDiagnosticCode() {
        return Multiline.prependStatusCodeToMessage(smtpReplyCode,
                getMessagePrefixedWithEnhancedStatusCode());
    }

    public RejectException createRejectException() {
        return new RejectException(getSmtpReplyCode(),
                getMessagePrefixedWithEnhancedStatusCode());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((enhancedStatusCode == null) ? 0
                                : enhancedStatusCode.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + smtpReplyCode;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EnhancedStatus other = (EnhancedStatus) obj;
        if (enhancedStatusCode == null) {
            if (other.enhancedStatusCode != null)
                return false;
        } else if (!enhancedStatusCode.equals(other.enhancedStatusCode))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (smtpReplyCode != other.smtpReplyCode)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return smtpReplyCode + " " + enhancedStatusCode + " " + message;
    }

    public static enum StatusClass {
        Success(1), TransientFailure(4), PermanentFailure(5);
        private final int code;

        StatusClass(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }
}
