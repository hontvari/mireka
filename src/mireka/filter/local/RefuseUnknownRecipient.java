package mireka.filter.local;

import mireka.destination.UnknownRecipientDestination;
import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.UnknownUserException;

/**
 * The RefuseUnknownRecipient filter rejects recipients whose destination has
 * not been set (null) or whose destination is
 * {@link UnknownRecipientDestination}. A destination must be assigned to the
 * recipient before the {@link #verifyRecipient} method of this class is called.
 * 
 * @see LookupDestinationFilter
 */
public class RefuseUnknownRecipient extends StatelessFilterType {

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        if (isKnown(recipientContext))
            return FilterReply.NEUTRAL;
        throw new UnknownUserException(recipientContext.recipient);
    }

    private boolean isKnown(RecipientContext recipientContext) {
        return recipientContext.isDestinationAssigned()
                && !(recipientContext.getDestination() instanceof UnknownRecipientDestination);
    }
}
