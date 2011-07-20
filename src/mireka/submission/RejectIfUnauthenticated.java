package mireka.submission;

import java.util.ArrayList;
import java.util.List;

import mireka.address.ReversePath;
import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.smtp.RejectExceptionExt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;

/**
 * Rejects the MAIL command if the session has not been authenticated.
 */
public class RejectIfUnauthenticated implements FilterType {
    private final List<MailTransactionSpecification> specifications =
            new ArrayList<MailTransactionSpecification>();

    public void addAuthenticatedSpecification(
            MailTransactionSpecification specification) {
        specifications.add(specification);
    }

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction);
    }

    private class FilterImpl extends AbstractFilter {
        private final Logger logger = LoggerFactory.getLogger(FilterImpl.class);

        public FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void from(ReversePath from) throws RejectExceptionExt {
            if (!isAuthenticated()) {
                logger.debug("None of the authentication specifications "
                        + "matched the session, rejecting");
                throw new RejectException(530, "Authentication required");
            }
            chain.from(from);
        }

        private boolean isAuthenticated() {
            for (MailTransactionSpecification spec : specifications) {
                if (spec.isSatisfiedBy(mailTransaction))
                    return true;
            }
            return false;
        }
    }
}