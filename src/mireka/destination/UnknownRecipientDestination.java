package mireka.destination;

import java.io.IOException;

import mireka.address.ReversePath;
import mireka.filter.RecipientContext;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.UnknownUserException;
import mireka.transmission.Mail;

/**
 * UnknownRecipientDestination is a special destination which rejects the
 * recipient in the {@link Session#recipient(RecipientContext)} phase.
 */
public class UnknownRecipientDestination implements SessionDestination {

    /**
     * Prevents creating an instance other then the default.
     */
    private UnknownRecipientDestination() {
        // nothing to do
    }

    @Override
    public Session createSession() {
        return new SessionImpl();
    }

    @Override
    public String toString() {
        return "UnknownRecipientDestination";
    }

    private class SessionImpl implements Session {

        @Override
        public void from(ReversePath from) throws RejectExceptionExt {
            // nothing to do
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            throw new UnknownUserException(recipientContext.recipient);
        }

        @Override
        public void data(Mail mail) throws RejectExceptionExt, IOException {
            // nothing to do

        }

        @Override
        public void done() {
            // nothing to do
        }
    }
}
