package mireka.submission;

import mireka.filter.MailTransaction;

import org.subethamail.smtp.AuthenticationHandler;

/**
 * The SmtpAuthenticated specification checks if a user in a MailTransaction is
 * authenticated using one of the available SMTP authentication methods
 * (typically with a name-password combination).
 */
public class SmtpAuthenticated implements MailTransactionSpecification {

    @Override
    public boolean isSatisfiedBy(MailTransaction mailTransaction) {
        AuthenticationHandler authenticationHandler =
                mailTransaction.getMessageContext().getAuthenticationHandler();
        return authenticationHandler != null;
    }
}
