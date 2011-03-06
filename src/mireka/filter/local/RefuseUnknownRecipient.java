package mireka.filter.local;

import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.filter.local.table.UnknownRecipientDestination;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.UnknownUserException;

/**
 * The RefuseUnknownRecipient filter rejects recipients whose destination has
 * been set to {@link UnknownRecipientDestination} . A destination must be assigned to the
 * recipient before the {@link #verifyRecipient} method of this class is called.
 * 
 * @see LookupDestination
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
        return !(recipientContext.getDestination() instanceof UnknownRecipientDestination);
    }
}
