package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.filter.Destination;

/**
 * An AliasDestination indicates that the final destination of mails sent to the
 * recipient should be the same as the destination assigned to another
 * recipient.
 */
public class AliasDestination implements Destination {

    private Recipient recipient;

    /**
     * @category GETSET
     */
    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    /**
     * @category GETSET
     */
    public Recipient getRecipient() {
        return recipient;
    }

    @Override
    public String toString() {
        return "AliasDestination [recipient=" + recipient + "]";
    }

}
