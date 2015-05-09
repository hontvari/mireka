package mireka.submission;

import java.util.ArrayList;
import java.util.List;

import mireka.filter.MailTransaction;
import mireka.filter.StatelessFilter;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rejects the MAIL command if the session has not been authenticated.
 * Authentication is typically be based on a name-password pair or the IP
 * address of the client, but these are configurable.
 */
public class RejectIfUnauthenticated extends StatelessFilter {
    private final Logger logger = LoggerFactory
            .getLogger(RejectIfUnauthenticated.class);

    private final List<MailTransactionSpecification> specifications =
            new ArrayList<MailTransactionSpecification>();

    /**
     * Sets the list of conditions for considering the client authenticated. If
     * any of the conditions is matched, than the user is considered to be
     * authenticated.
     */
    public void setAuthenticatedSpecifications(
            List<MailTransactionSpecification> specifications) {
        this.specifications.clear();
        this.specifications.addAll(specifications);
    }

    @Override
    public void from(MailTransaction transaction) throws RejectExceptionExt {
        if (!isAuthenticated(transaction)) {
            logger.debug("None of the authentication specifications "
                    + "matched the session, rejecting");
            throw new RejectExceptionExt(EnhancedStatus.AUTHENTICATION_REQUIRED);
        }
    }

    private boolean isAuthenticated(MailTransaction transaction) {
        for (MailTransactionSpecification spec : specifications) {
            if (spec.isSatisfiedBy(transaction))
                return true;
        }
        return false;
    }
}