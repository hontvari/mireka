package mireka.filter.local;

import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;

import org.subethamail.smtp.RejectException;

public class AcceptDomainPostmaster extends StatelessFilterType {

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
        if (recipientContext.recipient.isDomainPostmaster())
            return FilterReply.ACCEPT;
        return FilterReply.NEUTRAL;
    }
}
