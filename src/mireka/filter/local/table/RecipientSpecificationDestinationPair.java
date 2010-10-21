package mireka.filter.local.table;

import mireka.address.Recipient;
import mireka.address.RemotePartContainingRecipient;
import mireka.filter.Destination;

public class RecipientSpecificationDestinationPair implements
        RecipientDestinationMapper {
    private RecipientSpecification recipientSpecification;
    private Destination destination;

    @Override
    public Destination lookup(Recipient recipient) {
        if (!(recipient instanceof RemotePartContainingRecipient))
            return UnknownRecipient.INSTANCE;

        if (recipientSpecification
                .isSatisfiedBy((RemotePartContainingRecipient) recipient))
            return destination;
        else
            return UnknownRecipient.INSTANCE;
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
