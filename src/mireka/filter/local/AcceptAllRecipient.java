package mireka.filter.local;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;

import org.subethamail.smtp.RejectException;

/**
 * The AcceptAllRecipient filter accepts any recipient.
 */
public class AcceptAllRecipient extends StatelessFilter {

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectException {
        return RecipientVerificationResult.ACCEPT;
    }
}
