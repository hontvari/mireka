package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.destination.Destination;

/**
 * RecipientDestinationMapper maps recipients to destinations.
 */
public interface RecipientDestinationMapper {
    /**
     * Returns the destination of mails sent to this user; or null if it does
     * not know the recipient.
     */
    Destination lookup(Recipient recipient);
}
