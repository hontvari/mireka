package mireka.filter.local;

import mireka.ConfigurationException;
import mireka.address.Recipient;
import mireka.destination.AliasDestination;
import mireka.destination.Destination;
import mireka.filter.FilterSession;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;
import mireka.filter.local.table.RecipientDestinationMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;

/**
 * The LookupDestination filter assigns a destination to recipients in the
 * {@link FilterSession#verifyRecipient(RecipientContext)} phase. It resolves
 * aliases, so the destination set is never {@link AliasDestination}. It does
 * nothing if a destination is already assigned, except if it is an
 * {@link AliasDestination}, in which case it tries to resolve the alias.
 */
public class LookupDestinationFilter extends StatelessFilter {
    private Logger logger = LoggerFactory
            .getLogger(LookupDestinationFilter.class);

    private RecipientDestinationMapper recipientDestinationMapper;

    /**
     * @x.category GETSET
     */
    public RecipientDestinationMapper getRecipientDestinationMapper() {
        return recipientDestinationMapper;
    }

    /**
     * @x.category GETSET
     */
    public void setRecipientDestinationMapper(
            RecipientDestinationMapper recipientDestinationMapper) {
        this.recipientDestinationMapper = recipientDestinationMapper;
    }

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectException {
        Destination currentDestination =
                recipientContext.isDestinationAssigned() ? recipientContext
                        .getDestination() : null;
        if (currentDestination == null) {
            Destination destination =
                    lookupDestinationByResolvingAliases(recipientContext.recipient);
            recipientContext.setDestination(destination);
        } else if (currentDestination instanceof AliasDestination) {
            Destination destination =
                    lookupDestinationByResolvingAliases(((AliasDestination) currentDestination)
                            .getRecipient());
            recipientContext.setDestination(destination);
        }
        return RecipientVerificationResult.NEUTRAL;
    }

    private Destination lookupDestinationByResolvingAliases(Recipient recipient) {
        Destination destination;
        Recipient canonicalRecipient = recipient;
        int lookups = 0;
        while (true) {
            if (lookups > 10) {
                throw new ConfigurationException(
                        "Recipient aliases may created a loop for " + recipient);
            }
            destination = recipientDestinationMapper.lookup(canonicalRecipient);
            lookups++;
            if (destination instanceof AliasDestination) {
                canonicalRecipient =
                        ((AliasDestination) destination).getRecipient();
            } else {
                if (lookups > 1)
                    logger.debug("Final recipient is " + canonicalRecipient);
                break;
            }
        }
        return destination;
    }
}
