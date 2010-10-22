package mireka.filter.local;

import javax.enterprise.context.Dependent;

import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;

import org.subethamail.smtp.RejectException;

@Dependent
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
