package mireka.filter;

import mireka.ConfigurationException;
import mireka.address.Recipient;
import mireka.destination.Destination;

/**
 * RecipientContext collects information about a specific recipient during the
 * mail transaction.
 */
public class RecipientContext {
    private final MailTransaction mailTransaction;
    public final Recipient recipient;
    private Destination destination;
    /**
     * True if the mail has been passed to a reliable service, e.g. a back-end
     * SMTP server. This status flag is only used for recognizing and reporting
     * a wrong configuration, when delivery to a recipient is not handled by any
     * of the filters.
     */
    public boolean isResponsibilityTransferred;

    public RecipientContext(MailTransaction mailTransaction, Recipient recipient) {
        this.mailTransaction = mailTransaction;
        this.recipient = recipient;
    }

    public boolean isDestinationAssigned() {
        return destination != null;
    }

    /**
     * @category GETSET
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * @throws ConfigurationException
     *             if no destination is assigned yet
     */
    public Destination getDestination() throws ConfigurationException {
        if (destination == null)
            throw new ConfigurationException(
                    "Destination is not assigned to recipient " + recipient
                            + " yet, this is likely caused by "
                            + "wrong configuration");
        return destination;
    }

    /**
     * @category GETSET
     */
    public MailTransaction getMailTransaction() {
        return mailTransaction;
    }
}
