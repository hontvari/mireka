package mireka.filter;

import mireka.ConfigurationException;
import mireka.destination.Destination;
import mireka.smtp.address.Recipient;

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
     * @x.category GETSET
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
                    "Destination has not been assigned to recipient "
                            + recipient
                            + " (or to the final recipient if this "
                            + "is an alias) yet by the filter chain, "
                            + "this is likely caused by wrong configuration");
        return destination;
    }

    /**
     * @x.category GETSET
     */
    public MailTransaction getMailTransaction() {
        return mailTransaction;
    }
}
