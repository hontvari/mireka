package mireka.filter.local.table;

import java.util.ArrayList;
import java.util.List;

import mireka.address.Recipient;

/**
 * InlineRecipientRegistry is a convenience class, it makes easy to specify
 * valid addresses in the configuration files.
 */
public class InlineRecipientRegistry implements RecipientSpecification {
    private final List<RecipientSpecification> recipientSpecifications = new ArrayList<RecipientSpecification>();

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        for (RecipientSpecification specification : recipientSpecifications) {
            if (specification.isSatisfiedBy(recipient))
                return true;
        }
        return false;
    }

    public void addAddress(String address) {
        RecipientSpecification specification = new RecipientSpecificationFactory()
                .create(address);
        recipientSpecifications.add(specification);
    }

    public void setAddresses(List<String> addresses) {
        this.recipientSpecifications.clear();
        for (String address : addresses) {
            addAddress(address);
        }
    }
}
