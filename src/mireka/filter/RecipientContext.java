package mireka.filter;

import mireka.address.Recipient;

/**
 * RecipientContext collects information about a specific recipient during the
 * mail transaction.
 */
public class RecipientContext {
    public Recipient recipient;
    public Destination destination;
    /**
     * True if the mail has been passed to a reliable service, e.g. a back-end
     * SMTP server. This status flag is only used for recognizing and reporting
     * a wrong configuration, when delivery to a recipient is not handled by any
     * of the filters.
     */
    public boolean isResponsibilityTransferred;
}
