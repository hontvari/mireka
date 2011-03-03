package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.filter.Destination;

/**
 * RecipientSpecificationDestinationPair assigns a {@link Destination} to a
 * {@link RecipientSpecification}, it is used to configure what to do with mail
 * sent to the matching mail addresses.
 */
public class RecipientSpecificationDestinationPair implements
        RecipientDestinationMapper {
    private RecipientSpecification recipientSpecification;
    private Destination destination;

    @Override
    public Destination lookup(Recipient recipient) {
        if (recipientSpecification.isSatisfiedBy(recipient))
            return destination;
        else
            return UnknownRecipientDestination.INSTANCE;
    }

    /**
     * @category GETSET
     */
    public void setRecipientSpecification(
            RecipientSpecification recipientSpecification) {
        this.recipientSpecification = recipientSpecification;
    }

    /**
     * @category GETSET
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

}
