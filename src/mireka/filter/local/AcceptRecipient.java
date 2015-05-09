package mireka.filter.local;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;
import mireka.filter.local.table.RecipientSpecification;

import org.subethamail.smtp.RejectException;

/**
 * The AcceptRecipient filter accepts a recipient if it matches the configured
 * {@link RecipientSpecification}. If it does not match then if does not decide,
 * that is it does not reject the recipient.
 */
public class AcceptRecipient extends StatelessFilter {
    private RecipientSpecification recipientSpecification;

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectException {
        if (recipientSpecification.isSatisfiedBy(recipientContext.recipient))
            return RecipientVerificationResult.ACCEPT;
        else
            return RecipientVerificationResult.NEUTRAL;
    }

    /**
     * @x.category GETSET
     */
    public RecipientSpecification getRecipientSpecification() {
        return recipientSpecification;
    }

    /**
     * @x.category GETSET
     */
    public void setRecipientSpecification(
            RecipientSpecification recipientSpecification) {
        this.recipientSpecification = recipientSpecification;
    }

}
