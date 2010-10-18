package mireka.filter.local;

import java.util.ArrayList;
import java.util.List;

import org.subethamail.smtp.RejectException;

import mireka.UnknownUserException;
import mireka.address.Recipient;
import mireka.address.RemotePartContainingRecipient;
import mireka.filter.FilterReply;
import mireka.filter.StatelessFilterType;

public class RefuseUnknownRecipient extends StatelessFilterType {
    private List<RecipientSpecification> specifications =
            new ArrayList<RecipientSpecification>();

    public void addRecipientSpecification(RecipientSpecification specification) {
        specifications.add(specification);
    }

    @Override
    public FilterReply verifyRecipient(Recipient recipient)
            throws RejectException {
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
