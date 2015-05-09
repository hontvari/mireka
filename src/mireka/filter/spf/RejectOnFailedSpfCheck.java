package mireka.filter.spf;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.filter.StatelessFilter;
import mireka.smtp.RejectExceptionExt;

import org.apache.james.jspf.core.exceptions.SPFErrorConstants;
import org.apache.james.jspf.executor.SPFResult;
import org.subethamail.smtp.RejectException;

/**
 * The RejectOnFailedSpfCheck filter rejects any recipient if the SPF check
 * indicates that the client is not authorized to send mail in the name of the
 * publishing domain and the domain publishes a rejection policy for such mails.
 */
public class RejectOnFailedSpfCheck extends StatelessFilter {
    private boolean rejectOnPermanentError = false;

    /**
     * @x.category GETSET
     */
    public boolean isRejectOnPermanentError() {
        return rejectOnPermanentError;
    }

    /**
     * @x.category GETSET
     */
    public void setRejectOnPermanentError(boolean rejectOnPermanentError) {
        this.rejectOnPermanentError = rejectOnPermanentError;
    }

    @Override
    public RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectExceptionExt {
        SPFResult spfResult = new SpfChecker(transaction).getResult();
        String spfResultCode = spfResult.getResult();

        if (spfResultCode.equals(SPFErrorConstants.FAIL_CONV)) {
            String statusText;
            if (spfResult.getExplanation().isEmpty())
                statusText = "Blocked by SPF";
            else
                statusText = "Blocked - see: " + spfResult.getExplanation();
            throw new RejectException(550, statusText);
        } else if (spfResult.equals(SPFErrorConstants.TEMP_ERROR_CONV)) {
            throw new RejectException(451,
                    "Temporarily rejected: Problem on SPF lookup");
        } else if (rejectOnPermanentError
                && spfResultCode.equals(SPFErrorConstants.PERM_ERROR_CONV)) {
            throw new RejectException(550, "Blocked - invalid SPF record");
        } else {
            return RecipientVerificationResult.NEUTRAL;
        }
    }
}
