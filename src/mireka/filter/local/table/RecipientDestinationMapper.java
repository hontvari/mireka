package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.filter.Destination;

/**
 * RecipientDestinationMapper maps recipients to destinations.
 */
public interface RecipientDestinationMapper {
    /**
     * Returns {@link UnknownRecipientDestination} if it does not know the
     * recipient.
     */
    Destination lookup(Recipient recipient);
}
