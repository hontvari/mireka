package mireka.filter.local.table;

import java.util.ArrayList;
import java.util.List;

import mireka.address.RemotePartContainingRecipient;

public class RecipientSpecifications implements RecipientSpecification {
    private List<RecipientSpecification> specifications =
            new ArrayList<RecipientSpecification>();

    public void addSpecification(RecipientSpecification specification) {
        specifications.add(specification);
    }

    @Override
    public boolean isSatisfiedBy(RemotePartContainingRecipient recipient) {
        for (RecipientSpecification specification : specifications) {
            if (specification.isSatisfiedBy(recipient))
                return true;
        }
        return false;
    }
}
