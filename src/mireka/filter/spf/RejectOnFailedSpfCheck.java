package mireka.filter.spf;

import mireka.address.Recipient;
import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterReply;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;

import org.apache.james.jspf.core.exceptions.SPFErrorConstants;
import org.apache.james.jspf.executor.SPFResult;
import org.subethamail.smtp.RejectException;

public class RejectOnFailedSpfCheck implements FilterType {
    private boolean rejectOnPermanentError = false;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction, new SpfChecker(mailTransaction));
    }

    /**
     * @category GETSET
     */
    public boolean isRejectOnPermanentError() {
        return rejectOnPermanentError;
    }

    /**
     * @category GETSET
     */
    public void setRejectOnPermanentError(boolean rejectOnPermanentError) {
        this.rejectOnPermanentError = rejectOnPermanentError;
    }

    private class FilterImpl extends AbstractFilter {
        private final SpfChecker spfChecker;

        public FilterImpl(MailTransaction mailTransaction, SpfChecker spfChecker) {
            super(mailTransaction);
            this.spfChecker = spfChecker;
        }

        @Override
        public FilterReply verifyRecipient(Recipient recipient)
                throws RejectException {
            SPFResult spfResult = spfChecker.getResult();
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
            }

            return chain.verifyRecipient(recipient);
        }
    }
}
