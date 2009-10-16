package mireka.submission;

import org.subethamail.smtp.AuthenticationHandler;

import mireka.filter.MailTransaction;

public class SmtpAuthenticated implements MailTransactionSpecification {

    @Override
    public boolean isSatisfiedBy(MailTransaction mailTransaction) {
        AuthenticationHandler authenticationHandler = mailTransaction.getMessageContext().getAuthenticationHandler();
        return authenticationHandler != null;
    }
}
