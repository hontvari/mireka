package mireka.login;

import mireka.address.LocalPart;
import mireka.address.Recipient;
import mireka.destination.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;
import mireka.pop.MaildropDestination;
import mireka.pop.store.MaildropRepository;

/**
 * This class assigns a {@link MaildropDestination} to a {@link GlobalUsers}
 * user. The name of the maildrop is the same as the user's name.
 */
public class GlobalUsersMaildropDestinationMapper implements
        RecipientDestinationMapper {

    private GlobalUsers users;
    private MaildropRepository maildropRepository;

    @Override
    public Destination lookup(Recipient recipient) {
        LocalPart recipientLocalPart = recipient.localPart();
        for (GlobalUser user : users) {
            if (user.getUsernameObject().matches(recipientLocalPart)) {
                MaildropDestination destination = new MaildropDestination();
                destination
                        .setMaildropName(user.getUsernameObject().toString());
                destination.setMaildropRepository(maildropRepository);
                return destination;
            }
        }
        return null;

    }

    /**
     * GETSET
     */
    public void setUsers(GlobalUsers users) {
        this.users = users;
    }

    /**
     * @x.category GETSET
     */
    public void setMaildropRepository(MaildropRepository maildropRepository) {
        this.maildropRepository = maildropRepository;
    }

    /**
     * @x.category GETSET
     */
    public MaildropRepository getMaildropRepository() {
        return maildropRepository;
    }
}
