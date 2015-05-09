package mireka.filter.local;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;

import org.subethamail.smtp.RejectException;

/**
 * The AcceptPostmaster filter accepts the recipient if it is a postmaster
 * address, either a domain specific postmaster ("postmaster@example.com") or
 * the global postmaster ("postmaster", without domain part). It does not check
 * if the domain is local.
 */
public class AcceptPostmaster extends StatelessFilter {

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectException {
        if (recipientContext.recipient.isPostmaster())
            return RecipientVerificationResult.ACCEPT;
        else
            return RecipientVerificationResult.NEUTRAL;
    }
}
