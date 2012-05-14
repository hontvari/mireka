package mireka.forward;

import java.util.ArrayList;
import java.util.List;

import mireka.address.Recipient;
import mireka.destination.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;

/**
 * ForwardLists is a collection of forward lists, used in the configuration
 * files. It can also be used as a {@link RecipientDestinationMapper}.
 */
public class ForwardLists implements RecipientDestinationMapper {
    private final List<ForwardList> lists = new ArrayList<ForwardList>();

    @Override
    public Destination lookup(Recipient recipient) {
        for (ForwardList list : lists) {
            if (list.getRecipientSpecification().isSatisfiedBy(recipient)) {
                ForwardDestination destination = new ForwardDestination();
                destination.setList(list);
                return destination;
            }

        }
        return null;
    }

    public void addForwardList(ForwardList list) {
        lists.add(list);
    }

    public void setLists(List<ForwardList> lists) {
        this.lists.clear();
        this.lists.addAll(lists);
    }

}
