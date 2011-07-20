package mireka.filter.local;

import java.util.ArrayList;
import java.util.List;

import mireka.address.GlobalPostmaster;
import mireka.address.Recipient;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;
import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.filter.local.table.RemotePartSpecification;

import org.subethamail.smtp.RejectException;

/**
 * This filter rejects recipient addresses of which remote part is not a local
 * domain (or address literal). It does not rejects the special, global
 * postmaster address.
 */
public class ProhibitRelaying extends StatelessFilterType {
    private List<RemotePartSpecification> localDomainSpecifications =
            new ArrayList<RemotePartSpecification>();

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
        Recipient recipient = recipientContext.recipient;
        if (recipient instanceof GlobalPostmaster)
            return FilterReply.NEUTRAL;
        else if (recipient instanceof RemotePartContainingRecipient)
            return verifyRemotePartContainingRecipient((RemotePartContainingRecipient) recipient);
        else
            throw new IllegalArgumentException();
    }

    private FilterReply verifyRemotePartContainingRecipient(
            RemotePartContainingRecipient recipient) throws RejectException {
        RemotePart remotePart = recipient.getMailbox().getRemotePart();
        for (RemotePartSpecification remotePartSpecification : localDomainSpecifications) {
            if (remotePartSpecification.isSatisfiedBy(remotePart))
                return FilterReply.NEUTRAL;
        }
        throw new RejectException(550,
                "Relaying prohibited, user is not local (" + recipient + ")");

    }

    public void addLocalDomainSpecification(
            RemotePartSpecification remotePartSpecification) {
        if (remotePartSpecification == null)
            throw new NullPointerException();
        localDomainSpecifications.add(remotePartSpecification);
    }
}
