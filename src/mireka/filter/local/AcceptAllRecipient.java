package mireka.filter.local;

import mireka.address.Recipient;
import mireka.filter.FilterReply;
import mireka.filter.StatelessFilterType;

import org.subethamail.smtp.RejectException;

public class AcceptAllRecipient extends StatelessFilterType {

    @Override
    public FilterReply verifyRecipient(Recipient recipient)
            throws RejectException {
        return FilterReply.ACCEPT;
    }
}
