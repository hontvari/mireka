package mireka.filter.local.table;

import java.text.ParseException;

import mireka.destination.AliasDestination;
import mireka.destination.Destination;
import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Recipient;

/**
 * PostmasterAliasMapper is a convenience class used in configuration files to
 * assign a Postmaster alias. It maps the global postmaster and any domain
 * specific postmaster addresses to an {@link AliasDestination}.
 */
public class PostmasterAliasMapper implements RecipientDestinationMapper {
    private final RecipientSpecification postmasterSpecification =
            new AnyPostmaster();
    private AliasDestination destination;

    @Override
    public Destination lookup(Recipient recipient) {
        return postmasterSpecification.isSatisfiedBy(recipient) ? destination
                : null;
    }

    /**
     * @x.category GETSET
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
