package mireka.filter.local;

import mireka.ConfigurationException;
import mireka.address.Recipient;
import mireka.destination.AliasDestination;
import mireka.destination.Destination;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterBase;
import mireka.filter.FilterReply;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.local.table.RecipientDestinationMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;

/**
 * The LookupDestination filter assigns a destination to recipients in the
 * {@link FilterBase#verifyRecipient(RecipientContext)} phase. It resolves
 * aliases, so the destination set is never {@link AliasDestination}. It does
 * nothing if a destination is already assigned, except if it is an
 * {@link AliasDestination}, in which case it tries to resolve the alias.
 */
public class LookupDestinationFilter implements FilterType {
    private RecipientDestinationMapper recipientDestinationMapper;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        FilterImpl filterInstance = new FilterImpl(mailTransaction);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

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

    private class FilterImpl extends AbstractDataRecipientFilter {

        private Logger logger = LoggerFactory.getLogger(FilterImpl.class);

        public FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public FilterReply verifyRecipient(RecipientContext recipientContext)
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
            return FilterReply.NEUTRAL;
        }

        private Destination lookupDestinationByResolvingAliases(
                Recipient recipient) {
            Destination destination;
            Recipient canonicalRecipient = recipient;
            int lookups = 0;
            while (true) {
                if (lookups > 10) {
                    throw new ConfigurationException(
                            "Recipient aliases may created a loop for "
                                    + recipient);
                }
                destination =
                        recipientDestinationMapper.lookup(canonicalRecipient);
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
}
