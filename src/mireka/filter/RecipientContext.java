package mireka.filter;

import mireka.address.Recipient;

/**
 * RecipientContext collects information about a specific recipient during the
 * mail transaction.
 */
public class RecipientContext {
    public final Recipient recipient;
    private Destination destination;
    /**
     * True if the mail has been passed to a reliable service, e.g. a back-end
     * SMTP server. This status flag is only used for recognizing and reporting
     * a wrong configuration, when delivery to a recipient is not handled by any
     * of the filters.
     */
    public boolean isResponsibilityTransferred;

    public RecipientContext(Recipient recipient) {
        this.recipient = recipient;
    }

    /**
     * @category GETSET
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * @category GETSET
     */
    public Destination getDestination() {
        if (destination == null)
            throw new IllegalStateException(
                    "Destination is not assigned to recipient " + recipient
                            + " yet, this is likely caused by "
                            + "wrong configuration");
        return destination;
    }

}
