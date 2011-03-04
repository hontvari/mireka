package mireka.filter.local.table;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.ParseException;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.filter.Destination;

/**
 * AliasMapper is a convenience class used in configuration files to create an
 * alias. It maps a mail address to an {@link AliasDestination}.
 */
public class AliasMapper implements RecipientDestinationMapper {
    private final List<RecipientSpecification> aliases =
            new ArrayList<RecipientSpecification>();
    private AliasDestination destination;

    @Override
    public Destination lookup(Recipient recipient) {
        for (RecipientSpecification alias : aliases) {
            if (alias.isSatisfiedBy(recipient)) {
                return destination;
            }
        }
        return UnknownRecipientDestination.INSTANCE;
    }

    /**
     * GETSET
     */
    public void addAlias(String mailbox) {
        RecipientSpecification recipientSpecification =
                new RecipientSpecificationFactory().create(mailbox);
        aliases.add(recipientSpecification);
    }

    /**
     * GETSET
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

    @Override
    public String toString() {
        return "AliasMapper [alias=" + aliases + ", destination="
                + destination.getRecipient() + "]";
    }
}
