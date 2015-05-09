package mireka.filter.local;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;

import org.subethamail.smtp.RejectException;

/**
 * The AcceptGlobalPostmaster filter accepts the recipient if it is the global
 * postmaster address. The global postmaster address looks like "postmaster",
 * without a domain part.
 */
public class AcceptGlobalPostmaster extends StatelessFilter {

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectException {
        if (recipientContext.recipient.isGlobalPostmaster())
            return RecipientVerificationResult.ACCEPT;
        else
            return RecipientVerificationResult.NEUTRAL;
    }
}
