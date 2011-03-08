package mireka.forward;

import java.util.ArrayList;
import java.util.List;

import mireka.address.Recipient;
import mireka.filter.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;
import mireka.filter.local.table.UnknownRecipientDestination;

/**
 * ForwardLists is a collection of forward lists, used in the configuration
 * files. It can also be used as a {@link RecipientDestinationMapper}.
 */
public class ForwardLists implements RecipientDestinationMapper {
    private final List<ForwardList> lists = new ArrayList<ForwardList>();

    public void addForwardList(ForwardList list) {
        lists.add(list);
    }

    @Override
    public Destination lookup(Recipient recipient) {
        for (ForwardList list : lists) {
            if (list.getRecipientSpecification().isSatisfiedBy(recipient)) {
                ForwardDestination destination = new ForwardDestination();
                destination.setList(list);
                return destination;
            }

        }
        return UnknownRecipientDestination.INSTANCE;
    }

}
