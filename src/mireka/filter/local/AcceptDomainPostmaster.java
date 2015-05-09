package mireka.filter.local;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;

import org.subethamail.smtp.RejectException;

/**
 * The AcceptDomainPostmaster filters accepts a recipient if it is a full
 * postmaster address with a domain. It does not check if the domain is local.
 */
public class AcceptDomainPostmaster extends StatelessFilter {

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectException {
        if (recipientContext.recipient.isDomainPostmaster())
            return RecipientVerificationResult.ACCEPT;
        return RecipientVerificationResult.NEUTRAL;
    }
}
