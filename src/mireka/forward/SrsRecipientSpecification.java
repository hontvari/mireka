package mireka.forward;

import mireka.address.Recipient;
import mireka.filter.local.table.RecipientSpecification;

/**
 * Matches any recipient which seems to be an SRS reverse path. It does not
 * check its validity, that is the responsibility of the {@link SrsDestination}
 * class.
 */
public class SrsRecipientSpecification implements RecipientSpecification {

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        String localPart = recipient.localPart().smtpText();
        return Srs.SRS0_PREFIX.matcher(localPart).lookingAt()
                || Srs.SRS1_PREFIX.matcher(localPart).lookingAt();
    }

}
