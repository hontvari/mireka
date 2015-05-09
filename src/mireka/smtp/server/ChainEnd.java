package mireka.smtp.server;

import mireka.filter.FilterSession;
import mireka.filter.RecipientContext;
import mireka.filter.RecipientVerificationResult;

/**
 * ChainEnd is the closing element of a filter chain. It overrides the event
 * handling functions to not do anything, specifically to not try to call the
 * next link.
 */
class ChainEnd extends FilterSession {

    @Override
    public void begin() {
        // do nothing
    }

    @Override
    public void from() {
        // do nothing
    }

    @Override
    public RecipientVerificationResult verifyRecipient(
            RecipientContext recipientContext) {
        return RecipientVerificationResult.NEUTRAL;
    }

    @Override
    public void recipient(RecipientContext recipientContext) {
        // do nothing
    }

    @Override
    public void dataStream() {
        // do nothing
    }

    @Override
    public void data() {
        // do nothing
    }
}
