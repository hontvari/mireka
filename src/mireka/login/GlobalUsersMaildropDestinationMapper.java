package mireka.login;

import javax.inject.Inject;

import mireka.destination.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;
import mireka.imap.Settings;
import mireka.imap.SettingsRepo;
import mireka.pop.MaildropDestination;
import mireka.pop.store.MaildropRepository;
import mireka.smtp.address.LocalPart;
import mireka.smtp.address.Recipient;

/**
 * This class assigns a {@link MaildropDestination} to a global user, based solely on matching the
 * local part of the address.
 */
public class GlobalUsersMaildropDestinationMapper implements
        RecipientDestinationMapper {

    private Userlist users;
    @Inject
    public SettingsRepo settingsRepo;
    private MaildropRepository maildropRepository;

    @Override
    public Destination lookup(Recipient recipient) {
        LocalPart recipientLocalPart = recipient.localPart();
        for (User user : users) {
            Settings u = settingsRepo.get(user);
            if (u.ciName().matches(recipientLocalPart)) {
                MaildropDestination destination = new MaildropDestination();
                destination.setUser(u.user());
                destination.setMaildropRepository(maildropRepository);
                return destination;
            }
        }
        return null;

    }

    /**
     * GETSET
     */
    public void setUsers(Userlist users) {
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
