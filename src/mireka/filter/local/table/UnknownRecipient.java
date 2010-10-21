package mireka.filter.local.table;

import mireka.filter.Destination;

/**
 * UnknownRecipient is a special destination which indicates that a recipient is
 * not included in a {@link RecipientDestinationMapper}.
 */
public class UnknownRecipient implements Destination {

    public static UnknownRecipient INSTANCE = new UnknownRecipient();

    /**
     * Prevents creating an instance other then the default.
     */
    private UnknownRecipient() {
        // nothing to do
    }

}
