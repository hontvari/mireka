package mireka.filter.local;

import java.util.ArrayList;
import java.util.List;

import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;
import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.filter.local.table.DomainSpecification;

import org.subethamail.smtp.RejectException;

public class ProhibitRelaying extends StatelessFilterType {
    private List<DomainSpecification> localDomainSpecifications =
            new ArrayList<DomainSpecification>();

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
        if (recipientContext.recipient.isGlobalPostmaster())
            return FilterReply.NEUTRAL;
        else
            return verifyRemotePartContainingRecipient((RemotePartContainingRecipient) recipientContext);
    }

    private FilterReply verifyRemotePartContainingRecipient(
            RemotePartContainingRecipient recipient) throws RejectException {
        RemotePart remotePart = recipient.getAddress().getRemotePart();
        for (DomainSpecification domainSpecification : localDomainSpecifications) {
            if (domainSpecification.isSatisfiedBy(remotePart))
                return FilterReply.NEUTRAL;
        }
        throw new RejectException(550,
                "Relaying prohibited, user is not local (" + recipient + ")");

    }

    public void addLocalDomainSpecification(
            DomainSpecification domainSpecification) {
        if (domainSpecification == null)
            throw new NullPointerException();
        localDomainSpecifications.add(domainSpecification);
    }
}
