package mireka.filter;

/**
 * The possible results of the verifications of a recipient in a mail
 * transaction.
 * 
 * @see FilterSession#verifyRecipient(RecipientContext)
 */
public enum RecipientVerificationResult {
    /**
     * The recipient must accepted, no further verification is necessary, which
     * means that no further filters should be called.
     */
    ACCEPT,
    /**
     * It indicated that the current filter does not decide if the recipient
     * must be accepted or rejected, it lefts the decision to later filters in
     * the chain.
     */
    NEUTRAL;
}
