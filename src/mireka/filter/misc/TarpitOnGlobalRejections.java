package mireka.filter.misc;

import mireka.filter.Filter;
import mireka.filter.FilterSession;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.UnknownUserException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TarpitOnGlobalRejections filter slows down replies to RCPT command on all
 * connections if unknown recipients are submitted by a client.
 */
public class TarpitOnGlobalRejections implements Filter {
    private final Logger logger = LoggerFactory
            .getLogger(TarpitOnGlobalRejections.class);
    private final Tarpit tarpit = new Tarpit();

    @Override
    public FilterSession createSession() {
        return new FilterImpl();
    }

    private class FilterImpl extends FilterSession {

        @Override
        public RecipientVerificationResult verifyRecipient(
                RecipientContext recipientContext) throws RejectExceptionExt {
            try {
                return super.verifyRecipient(recipientContext);
            } catch (UnknownUserException e) {
                tarpit.addRejection();
                sleep();
                throw e;
            }
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            try {
                super.recipient(recipientContext);
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
