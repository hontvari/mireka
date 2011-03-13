package mireka.list;

import java.util.ArrayList;
import java.util.List;

import mireka.address.Recipient;
import mireka.destination.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;

/**
 * MailingLists is a collection of mailing lists, used in the configuration
 * files. It can also be used as a {@link RecipientDestinationMapper}.
 */
public class MailingLists implements RecipientDestinationMapper {
    private final List<MailingList> mailingLists = new ArrayList<MailingList>();

    public void addMailingList(MailingList mailingList) {
        mailingLists.add(mailingList);
    }

    @Override
    public Destination lookup(Recipient recipient) {
        for (MailingList list : mailingLists) {
            if (list.getRecipientSpecification().isSatisfiedBy(recipient)) {
                ListDestination destination = new ListDestination();
                destination.setList(list);
                return destination;
            }

        }
        return null;
    }

}
