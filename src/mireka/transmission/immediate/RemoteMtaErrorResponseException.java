package mireka.transmission.immediate;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.MailSystemStatus;
import mireka.smtp.SendException;
import mireka.smtp.client.MtaAddress;

import org.subethamail.smtp.client.SMTPException;

/**
 * Thrown to indicate that the remote MTA returned an error message.
 */
public class RemoteMtaErrorResponseException extends SendException {
    private static final long serialVersionUID = -2886452940130526142L;

    private final MtaAddress remoteMta;

    private final MailSystemStatus remoteMtaStatus;

    public RemoteMtaErrorResponseException(SMTPException e, MtaAddress remoteMta) {
        super(e, enhancedStatusFromRemoteResponse(smtpStatusFromResponse(e)));
        this.remoteMta = remoteMta;
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

    public MtaAddress remoteMta() {
        return remoteMta;
    }

    /**
     * SMTP response sent by the remote MTA
     */
    public MailSystemStatus remoteMtaStatus() {
        return remoteMtaStatus;
    }
}
