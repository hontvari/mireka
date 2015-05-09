package mireka.list;

import mireka.destination.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;
import mireka.filter.local.table.RecipientSpecification;
import mireka.filter.local.table.RecipientSpecificationFactory;
import mireka.smtp.address.Recipient;

/**
 * ListMapper is a configuration helper, which maps the canonical address of the
 * list (in case-insensitive mode) to the list.
 */
public class ListMapper implements RecipientDestinationMapper {
    private RecipientSpecification recipientSpecification;
    private ListDestination list;

    @Override
    public Destination lookup(Recipient recipient) {
        if (recipientSpecification.isSatisfiedBy(recipient))
            return list;
        return null;
    }

    public void setList(ListDestination list) {
        this.recipientSpecification =
                new RecipientSpecificationFactory().create(list.getAddress());
        this.list = list;
    }
}
