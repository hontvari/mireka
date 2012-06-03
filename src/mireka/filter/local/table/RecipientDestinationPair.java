package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.destination.Destination;

/**
 * RecipientDestinationPair is a configuration helper object which assigns a
 * destination to a mailbox address which is supplied in text form.
 */
public class RecipientDestinationPair implements RecipientDestinationMapper {
    private RecipientSpecification recipientSpecification;
    private Destination destination;

    @Override
    public Destination lookup(Recipient recipient) {
        if (recipientSpecification.isSatisfiedBy(recipient))
            return destination;
        return null;
    }

    public void setRecipient(String recipient) {
        this.recipientSpecification =
                new RecipientSpecificationFactory().create(recipient);
    }

    /**
     * @category GETSET
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
