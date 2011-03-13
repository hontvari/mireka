package mireka.filter.local.table;

import javax.mail.internet.ParseException;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.destination.AliasDestination;
import mireka.destination.Destination;

/**
 * PostmasterAliasMapper is a convenience class used in configuration files to
 * assign a Postmaster alias. It maps the global postmaster and the local domain
 * postmaster addresses to an {@link AliasDestination}.
 */
public class PostmasterAliasMapper implements RecipientDestinationMapper {
    private LocalPostmaster localPostmasterSpecification;
    private AliasDestination destination;

    @Override
    public Destination lookup(Recipient recipient) {
        return localPostmasterSpecification.isSatisfiedBy(recipient) ? destination
                : null;
    }

    /**
     * @category GETSET
     */
    public void setLocalDomains(RemotePartSpecification localDomains) {
        localPostmasterSpecification = new LocalPostmaster();
        localPostmasterSpecification.setLocalDomains(localDomains);
    }

    /**
     * @category GETSET
     */
    public void setCanonical(String mailbox) {
        destination = new AliasDestination();
        try {
            destination.setRecipient(new MailAddressFactory()
                    .createRecipient(mailbox));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
