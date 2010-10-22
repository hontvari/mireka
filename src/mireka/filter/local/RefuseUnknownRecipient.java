package mireka.filter.local;

import java.util.ArrayList;
import java.util.List;

import mireka.UnknownUserException;
import mireka.address.Recipient;
import mireka.address.RemotePartContainingRecipient;
import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.filter.local.table.RecipientSpecification;

import org.subethamail.smtp.RejectException;

public class RefuseUnknownRecipient extends StatelessFilterType {
    private List<RecipientSpecification> specifications =
            new ArrayList<RecipientSpecification>();

    public void addRecipientSpecification(RecipientSpecification specification) {
        specifications.add(specification);
    }

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
        Recipient recipient = recipientContext.recipient;
        if (isKnown(recipient))
            return FilterReply.NEUTRAL;
        throw new UnknownUserException(recipient);
    }

    private boolean isKnown(Recipient recipient) {
        if (recipient.isGlobalPostmaster())
            return true;
        RemotePartContainingRecipient remotePartContainingRecipient =
                (RemotePartContainingRecipient) recipient;
        for (RecipientSpecification specification : specifications) {
            if (specification.isSatisfiedBy(remotePartContainingRecipient))
                return true;
        }
        return false;
    }
}
