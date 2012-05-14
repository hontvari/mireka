package mireka.filter.local;

import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;

import org.subethamail.smtp.RejectException;

public class AcceptPostmaster extends StatelessFilterType {

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
        if (recipientContext.recipient.isPostmaster())
            return FilterReply.ACCEPT;
        else
            return FilterReply.NEUTRAL;
    }
}
