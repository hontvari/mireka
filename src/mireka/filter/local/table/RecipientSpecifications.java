package mireka.filter.local.table;

import java.util.ArrayList;
import java.util.List;

import mireka.address.Recipient;

/**
 * RecipientSpecifications groups {@link RecipientSpecification} instances, it
 * matches a recipient if any of its elements matches it. It is itself a
 * RecipientSpecification.
 */
public class RecipientSpecifications implements RecipientSpecification {
    private List<RecipientSpecification> specifications =
            new ArrayList<RecipientSpecification>();

    public void addSpecification(RecipientSpecification specification) {
        specifications.add(specification);
    }

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        for (RecipientSpecification specification : specifications) {
            if (specification.isSatisfiedBy(recipient))
                return true;
        }
        return false;
    }
}
