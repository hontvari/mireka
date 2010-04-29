package mireka.transmission.immediate;

import mireka.transmission.EnhancedStatus;
import mireka.transmission.MailSystemStatus;

import org.subethamail.smtp.client.SMTPException;

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

    @Override
    public String toString() {
        return super.toString() + " " + remoteMtaStatus;
    }
}
