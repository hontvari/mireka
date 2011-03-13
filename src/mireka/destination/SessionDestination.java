package mireka.destination;

/**
 * SessionDestination is a {@link ResponsibleDestination}, which is able the
 * deliver the mail while following the mail transaction step by step.
 */
public interface SessionDestination extends ResponsibleDestination {
    /**
     * Creates a session object which will be notified about all steps of an
     * SMTP mail transaction.
     */
    Session createSession();
}
