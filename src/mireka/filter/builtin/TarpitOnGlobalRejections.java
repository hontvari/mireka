package mireka.filter.builtin;

import mireka.UnknownUserException;
import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterReply;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.mailaddress.Recipient;
import net.jcip.annotations.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;

/**
 * slows down replies to RCPT command on all connections
 */
public class TarpitOnGlobalRejections implements FilterType {
    private final Logger logger =
            LoggerFactory.getLogger(TarpitOnGlobalRejections.class);
    @GuardedBy(value = "itself")
    private final Tarpit tarpit = new Tarpit();

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction);
    }

    private class FilterImpl extends AbstractFilter {

        protected FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public FilterReply verifyRecipient(Recipient recipient)
                throws RejectException {
            try {
                return chain.verifyRecipient(recipient);
            } catch (UnknownUserException e) {
                tarpit.addRejection();
                sleep();
                throw e;
            }
        }

        @Override
        public void recipient(Recipient recipient) throws RejectException {
            try {
                chain.recipient(recipient);
            } catch (UnknownUserException e) {
                tarpit.addRejection();
                throw e;
            } finally {
                sleep();
            }
        }

        private void sleep() {
            try {
                long duration = tarpit.waitDuration();
                if (duration > 0) {
                    logger.debug("Sleeping {} ms", duration);
                    Thread.sleep(duration);
                }
            } catch (InterruptedException e) {
                // if something is so important, just continue
                Thread.currentThread().interrupt();
            }
        }
    }
}
