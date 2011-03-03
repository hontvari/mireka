package mireka.login;

import mireka.address.LocalPart;
import mireka.address.Recipient;
import mireka.filter.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;
import mireka.filter.local.table.UnknownRecipientDestination;
import mireka.pop.MaildropDestination;

/**
 * This class assigns a {@link MaildropDestination} to a {@link GlobalUsers}
 * user. The name of the maildrop is the same as the user's name.
 */
public class GlobalUsersMaildropDestinationMapper implements
        RecipientDestinationMapper {

    private GlobalUsers users;

    @Override
    public Destination lookup(Recipient recipient) {
        LocalPart recipientLocalPart = recipient.localPart();
        for (GlobalUser user : users) {
            if (user.getUsername().matches(recipientLocalPart)) {
                MaildropDestination destination = new MaildropDestination();
                destination.setMaildropName(user.getUsername().toString());
                return destination;
            }
        }
        return UnknownRecipientDestination.INSTANCE;

    }

    /**
     * GETSET
     */
    public void setUsers(GlobalUsers users) {
        this.users = users;
    }
}
