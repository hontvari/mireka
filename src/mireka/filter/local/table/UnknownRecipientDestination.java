package mireka.filter.local.table;

import mireka.filter.Destination;

/**
 * UnknownRecipientDestination is a special destination which indicates that a
 * recipient is not included in a {@link RecipientDestinationMapper}.
 */
public class UnknownRecipientDestination implements Destination {

    public static UnknownRecipientDestination INSTANCE =
            new UnknownRecipientDestination();

    /**
     * Prevents creating an instance other then the default.
     */
    private UnknownRecipientDestination() {
        // nothing to do
    }

}
