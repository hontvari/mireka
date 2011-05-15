package mireka.destination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mireka.MailData;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;

/**
 * DestinationProcessorFilter groups recipients by their destinations and calls
 * the {@link MailDestination} or {@link SessionDestination} objects with the
 * recipients to which they are assigned.
 */
public class DestinationProcessorFilter implements FilterType {

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        FilterImpl filterInstance = new FilterImpl(mailTransaction);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

    private class FilterImpl extends AbstractDataRecipientFilter {
        private final Logger logger = LoggerFactory.getLogger(FilterImpl.class);
        private final Map<ResponsibleDestination, DestinationState> destinations =
                new LinkedHashMap<ResponsibleDestination, DestinationState>();

        private Mail mail = new Mail();

        protected FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void from(String from) {
            mail.from = from;
            mail.receivedFromMtaAddress =
                    mailTransaction.getRemoteInetAddress();
            mail.receivedFromMtaName =
                    mailTransaction.getMessageContext().getHelo();
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            if (recipientContext.isResponsibilityTransferred)
                return;
            if (!(recipientContext.getDestination() instanceof ResponsibleDestination))
                return;
            ResponsibleDestination destination =
                    (ResponsibleDestination) recipientContext.getDestination();
            recipientContext.isResponsibilityTransferred = true;
            DestinationState destinationState =
                    getOrCreateDestinationState(destination);

            destinationState.recipient(recipientContext);
        }

        private DestinationState getOrCreateDestinationState(
                ResponsibleDestination destination) {
            DestinationState destinationState = destinations.get(destination);
            if (destinationState == null) {
                if (destination instanceof MailDestination) {
                    destinationState =
                            new MailDestinationState(
                                    (MailDestination) destination);
                } else if (destination instanceof SessionDestination) {
                    destinationState =
                            new SessionDestinationState(
                                    (SessionDestination) destination);
                } else {
                    throw new RuntimeException("Assertion failed");
                }
                destinations.put(destination, destinationState);
            }
            return destinationState;
        }

        @Override
        public void data(MailData data) throws TooMuchDataException,
                RejectExceptionExt, IOException {
            mail.mailData = data;
            mail.arrivalDate = new Date();
            mail.scheduleDate = mail.arrivalDate;
            for (Map.Entry<ResponsibleDestination, DestinationState> entry : destinations
                    .entrySet()) {
                ResponsibleDestination destination = entry.getKey();
                DestinationState destinationState = entry.getValue();
                Mail destinationMail = mail.copy();
                List<RecipientContext> recipientContexts =
                        destinationState.recipientContexts;
                for (RecipientContext recipientContext : recipientContexts) {
                    destinationMail.recipients.add(recipientContext.recipient);
                }
                if (recipientContexts.isEmpty()) {
                    logger.debug("Destination {} has not accepted any of the "
                            + "recipients to which it was assigned.",
                            destination);
                } else {
                    destinationState.data(destinationMail);
                }
            }
        }

        @Override
        public void done() {
            for (DestinationState destinationState : destinations.values()) {
                destinationState.done();
            }
        }

        private abstract class DestinationState {
            final List<RecipientContext> recipientContexts =
                    new ArrayList<RecipientContext>();

            abstract void recipient(RecipientContext recipientContext)
                    throws RejectExceptionExt;

            abstract void data(Mail mail) throws RejectExceptionExt,
                    IOException;

            /**
             * Safely closes the session.
             */
            abstract void done();
        }

        private class MailDestinationState extends DestinationState {
            MailDestination destination;

            public MailDestinationState(MailDestination destination) {
                this.destination = destination;
            }

            @Override
            void recipient(RecipientContext recipientContext) {
                recipientContexts.add(recipientContext);
            }

            @Override
            void data(Mail mail) throws RejectExceptionExt {
                logger.debug("Sending {} recipients to {}",
                        recipientContexts.size(), destination);
                destination.data(mail);
            }

            @Override
            void done() {
                // do nothing
            }
        }

        private class SessionDestinationState extends DestinationState {
            final SessionDestination destination;
            final Session session;
            boolean fromCalled = false;

            public SessionDestinationState(SessionDestination destination) {
                this.destination = destination;
                this.session = destination.createSession();
            }

            @Override
            void recipient(RecipientContext recipientContext)
                    throws RejectExceptionExt {
                if (!fromCalled) {
                    fromCalled = true;
                    session.from(mail.from);
                }
                session.recipient(recipientContext);
                recipientContexts.add(recipientContext);
            }

            @Override
            void data(Mail mail) throws RejectExceptionExt, IOException {
                logger.debug("Sending data for {} recipients to {}",
                        recipientContexts.size(), destination);
                session.data(mail);
            }

            @Override
            void done() {
                try {
                    session.done();
                } catch (RuntimeException e) {
                    logger.error(
                            "Cleanup of session failed for " + destination, e);
                }
            }
        }
    }
}
