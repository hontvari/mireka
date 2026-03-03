package mireka.login;

import javax.inject.Inject;

import mireka.destination.CombinedMailDestination;
import mireka.destination.Destination;
import mireka.filter.local.table.RecipientDestinationMapper;
import mireka.imap.ImapDestination;
import mireka.imap.Settings;
import mireka.imap.SettingsRepo;
import mireka.imap.store.Repository;
import mireka.pop.MaildropDestination;
import mireka.pop.store.MaildropRepository;
import mireka.smtp.address.LocalPart;
import mireka.smtp.address.Recipient;

/**
 * This class assigns a {@link MaildropDestination} and an {@link ImapDestination} to a global user,
 * based solely on matching the local part of the address.
 */
public class GlobalUsersPopImapDestinationMapper implements
        RecipientDestinationMapper {

    private Userlist users;
    @Inject
    public SettingsRepo settingsRepo;
    private MaildropRepository popRepository;
    private Repository imapRepository;

    @Override
    public Destination lookup(Recipient recipient) {
        LocalPart recipientLocalPart = recipient.localPart();
        for (User user : users) {
            Settings u = settingsRepo.get(user);
            if (u.ciName().matches(recipientLocalPart)) {
                CombinedMailDestination d = new CombinedMailDestination();

                MaildropDestination popDestination = new MaildropDestination();
                popDestination.setUser(u.user());
                popDestination.setMaildropRepository(popRepository);
                d.setD1(popDestination);

                ImapDestination imapDestination = new ImapDestination();
                imapDestination.repository = imapRepository;
                imapDestination.user = u.user();
                d.setD2(imapDestination);

                return d;
            }
        }
        return null;

    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setUsers(UserRepo users) {
        this.users = users;
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setMaildropRepository(MaildropRepository maildropRepository) {
        this.popRepository = maildropRepository;
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setImapRepository(Repository imapRepository) {
        this.imapRepository = imapRepository;
    }

}
