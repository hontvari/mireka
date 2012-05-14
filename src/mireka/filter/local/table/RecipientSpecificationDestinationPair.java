package mireka.filter.local.table;

import java.util.ArrayList;
import java.util.List;

import mireka.address.Recipient;
import mireka.destination.Destination;

/**
 * RecipientSpecificationDestinationPair assigns a {@link Destination} to one or
 * more {@link RecipientSpecification}, it is used to configure what to do with
 * mail sent to the matching mail addresses.
 */
public class RecipientSpecificationDestinationPair implements
        RecipientDestinationMapper {
    private final List<RecipientSpecification> recipientSpecifications = new ArrayList<RecipientSpecification>();
    private Destination destination;

    @Override
    public Destination lookup(Recipient recipient) {
        for (RecipientSpecification recipientSpecification : recipientSpecifications) {
            if (recipientSpecification.isSatisfiedBy(recipient))
                return destination;
        }
        return null;
    }

    /**
     * @category GETSET
     */
    public void addRecipientSpecification(
            RecipientSpecification recipientSpecification) {
        this.recipientSpecifications.add(recipientSpecification);
    }

    /**
     * @category GETSET
     */
    public void setRecipientSpecification(
            RecipientSpecification recipientSpecification) {
        this.recipientSpecifications.clear();
        this.recipientSpecifications.add(recipientSpecification);
    }

    /**
     * @category GETSET
     */
    public void setRecipientSpecifications(
            List<RecipientSpecification> recipientSpecifications) {
        this.recipientSpecifications.clear();
        for (RecipientSpecification recipientSpecification : recipientSpecifications) {
            addRecipientSpecification(recipientSpecification);
        }
    }

    /**
     * @category GETSET
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

}
