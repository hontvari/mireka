package mireka.transmission.immediate;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.MailSystemStatus;

import org.subethamail.smtp.client.SMTPException;

/**
 * Thrown to indicate that the remote MTA returned an error message. 
 */
public class RemoteMtaErrorResponseException extends SendException {
    private static final long serialVersionUID = -2886452940130526142L;
    private final MailSystemStatus remoteMtaStatus;

    public RemoteMtaErrorResponseException(SMTPException e, RemoteMta remoteMta) {
        super(e, enhancedStatusFromRemoteResponse(smtpStatusFromResponse(e)),
                remoteMta);
        this.remoteMtaStatus = smtpStatusFromResponse(e);
    }

    private static MailSystemStatus smtpStatusFromResponse(SMTPException e) {
        return new ResponseParser()
                .createResponseLookingForEnhancedStatusCode(e.getResponse());
    }

    private static EnhancedStatus enhancedStatusFromRemoteResponse(
            MailSystemStatus smtpStatus) {
        if (smtpStatus instanceof EnhancedStatus) {
            return (EnhancedStatus) smtpStatus;
        } else if (smtpStatus instanceof Rfc821Status) {
            return new EnhancedStatus((Rfc821Status) smtpStatus);
        } else {
            throw new RuntimeException("Unexpected: " + smtpStatus.getClass());
        }
    }

    /**
     * SMTP response sent by the remote MTA
     */
    public MailSystemStatus remoteMtaStatus() {
        return remoteMtaStatus;
    }
}
