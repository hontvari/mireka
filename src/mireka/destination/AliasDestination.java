package mireka.destination;

import java.text.ParseException;

import mireka.ConfigurationException;
import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Recipient;

/**
 * An AliasDestination indicates that the final destination of mails sent to the
 * recipient should be the same as the destination assigned to another
 * recipient.
 */
public class AliasDestination implements Destination {

    private Recipient recipient;

    /**
     * @x.category GETSET
     */
    public void setRecipient(String recipient) {
        try {
            this.recipient =
                    new MailAddressFactory().createRecipient(recipient);
        } catch (ParseException e) {
            throw new ConfigurationException();
        }
    }

    /**
     * @x.category GETSET
     */
    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    /**
     * @x.category GETSET
     */
    public Recipient getRecipient() {
        return recipient;
    }

    @Override
    public String toString() {
        return "AliasDestination [recipient=" + recipient + "]";
    }

}
