package mireka.transmission.immediate;

import mireka.smtp.SendException;
import mireka.smtp.address.Recipient;

/**
 * RecipientRejection contains the rejected recipient and the rejection 
 * exception. 
 */
public class RecipientRejection {
    public final Recipient recipient;
    public final SendException sendException;

    public RecipientRejection(Recipient recipient,
            RemoteMtaErrorResponseException sendException) {
        this.recipient = recipient;
        this.sendException = sendException;
    }

    @Override
    public String toString() {
        return "RecipientRejection [" + recipient + ", " + sendException + "]";
    }

}
