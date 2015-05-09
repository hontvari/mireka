package mireka.filter.local;

import java.util.ArrayList;
import java.util.List;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;
import mireka.filter.local.table.RemotePartSpecification;
import mireka.smtp.address.GlobalPostmaster;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.RemotePart;
import mireka.smtp.address.RemotePartContainingRecipient;

import org.subethamail.smtp.RejectException;

/**
 * This filter rejects recipient addresses of which remote part is not a local
 * domain (or address literal). It does not rejects the special, global
 * postmaster address.
 */
public class ProhibitRelaying extends StatelessFilter {
    private List<RemotePartSpecification> localDomainSpecifications =
            new ArrayList<RemotePartSpecification>();

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectException {
        Recipient recipient = recipientContext.recipient;
        if (recipient instanceof GlobalPostmaster)
            return RecipientVerificationResult.NEUTRAL;
        else if (recipient instanceof RemotePartContainingRecipient)
            return verifyRemotePartContainingRecipient((RemotePartContainingRecipient) recipient);
        else
            throw new IllegalArgumentException();
    }

    private RecipientVerificationResult verifyRemotePartContainingRecipient(
            RemotePartContainingRecipient recipient) throws RejectException {
        RemotePart remotePart = recipient.getMailbox().getRemotePart();
        for (RemotePartSpecification remotePartSpecification : localDomainSpecifications) {
            if (remotePartSpecification.isSatisfiedBy(remotePart))
                return RecipientVerificationResult.NEUTRAL;
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

    public void setLocalDomainSpecifications(
            List<RemotePartSpecification> specifications) {
        this.localDomainSpecifications.clear();
        for (RemotePartSpecification specification : specifications)
            addLocalDomainSpecification(specification);
    }
}
