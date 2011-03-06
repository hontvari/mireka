package mireka.transmission.immediate;

import mireka.smtp.MailSystemStatus;
import mireka.util.Multiline;

import org.subethamail.smtp.client.SMTPClient.Response;

/**
 * This class represents an SMTP status which does not include enhanced status
 * code.
 */
public class Rfc821Status implements MailSystemStatus {
    private final Response response;

    public Rfc821Status(Response response) {
        this.response = response;
    }

    @Override
    public int getSmtpReplyCode() {
        return response.getCode();
    }

    public String getMessage() {
        return response.getMessage();
    }

    @Override
    public String getDiagnosticCode() {
        return Multiline.prependStatusCodeToMessage(response.getCode(),
                response.getMessage());
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
