package mireka.pop.store;

/**
 * Signals a maildrop store related problem, which does not specific either to
 * POP3 (retrieving mail) or SMTP (putting mail in).
 */
public class MaildropException extends Exception {
    private static final long serialVersionUID = 3752919135764996587L;

    public MaildropException() {
        super();
    }

    public MaildropException(String message) {
        super(message);
    }

    public MaildropException(Throwable cause) {
        super(cause);
    }

    public MaildropException(String message, Throwable cause) {
        super(message, cause);
    }

}
