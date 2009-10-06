package mireka.filter.builtin.local;

import mireka.filter.FilterReply;
import mireka.filter.StatelessFilterType;
import mireka.mailaddress.Recipient;

import org.subethamail.smtp.RejectException;

public class AcceptDomainPostmaster extends StatelessFilterType {

    @Override
    public FilterReply verifyRecipient(Recipient recipient)
            throws RejectException {
        if (recipient.isDomainPostmaster())
            return FilterReply.ACCEPT;
        return FilterReply.NEUTRAL;
    }
}
