package mireka.filter.local.table;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.destination.AliasDestination;
import mireka.destination.Destination;
import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Recipient;

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
        return null;
    }

    /**
     * GETSET
     */
    public void addAlias(String mailbox) {
        RecipientSpecification recipientSpecification =
                new RecipientSpecificationFactory().create(mailbox);
        aliases.add(recipientSpecification);
    }

    public void setAliases(List<String> aliases) {
        this.aliases.clear();
        for (String alias : aliases)
            addAlias(alias);
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
