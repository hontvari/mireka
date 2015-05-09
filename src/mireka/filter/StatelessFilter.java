package mireka.filter;

import mireka.maildata.io.MaildataFileReadException;
import mireka.smtp.RejectExceptionExt;
import mireka.util.AssertionException;

/**
 * StatelessFilter is a base class which can be extended by filters to create
 * simple filters which are stateless and which do not depend on the result of
 * the following filters in the chain. Here stateless means that no state
 * information is exchanged between the mail transaction steps within the
 * filter. However, the filter does have access to all mail transaction data.
 * 
 * Note: implementing classes must be thread safe, because they can be used by
 * multiple connections at the same time.
 */
public abstract class StatelessFilter implements Filter {

    @Override
    public FilterSession createSession() {
        return new Session();
    }

    /**
     * It is called at the start of the mail transaction, it may store some data
     * in the specified transaction object, which may be useful for other
     * filters.
     * 
     * It corresponds to the {@link FilterSession#begin()} function of stateful
     * filters, see that for more information.
     * 
     * @param transaction
     *            The mail transaction which is in progress.
     * 
     * @see FilterSession#begin()
     */
    protected void begin(MailTransaction transaction) {
        // do nothing
    }

    /**
     * It is called after the MAIL command is received, before the answer is
     * sent, it may check and reject the command.
     * 
     * It corresponds to the {@link FilterSession#from()} function of stateful
     * filters, see that for more information.
     * 
     * @param transaction
     *            The mail transaction which is in progress.
     * @throws RejectExceptionExt
     *             if mail from the specified reverse-path or at least the MAIL
     *             FROM command in question must be rejected.
     * 
     * @see FilterSession#from()
     */
    protected void from(MailTransaction transaction) throws RejectExceptionExt {
        // do nothing
    }

    /**
     * It is called after the RCPT command is received, it may check the
     * recipient, and accept is, reject it, or leave the decision to later
     * filters in the chain.
     * 
     * It corresponds to the
     * {@link FilterSession#verifyRecipient(RecipientContext)} function of
     * stateful filters, see that for more information.
     * 
     * @param transaction
     *            The mail transaction which is in progress.
     * @throws RejectExceptionExt
     *             if the recipient is not valid and it must be rejected, or the
     *             server does not accept mail from the remote server at all,
     *             except maybe mail addressed to the postmaster.
     * 
     * @see FilterSession#verifyRecipient(RecipientContext)
     */
    protected RecipientVerificationResult verifyRecipient(
            MailTransaction transaction, RecipientContext recipientContext)
            throws RejectExceptionExt {
        return RecipientVerificationResult.NEUTRAL;
    }

    /**
     * It is called after the RCPT command, but only if one of the filters
     * accepted the recipient in their <code>verifyRecipient</code> function.
     * 
     * It corresponds to the {@link FilterSession#recipient(RecipientContext)}
     * function of stateful filters, see that for more information.
     * 
     * @param transaction
     *            The mail transaction which is in progress.
     * 
     * @throws RejectExceptionExt
     *             if the RCPT TO statement must be rejected for example because
     *             the proxy backend rejected. This should be rare, because the
     *             {@link #verifyRecipient(RecipientContext)} operation already
     *             checked the recipient.
     * 
     * @see FilterSession#recipient(RecipientContext)
     */
    protected void recipient(MailTransaction transaction,
            RecipientContext recipientContext) throws RejectExceptionExt {
        // do nothing
    }

    /**
     * It is called after the DATA command is accepted, before this server
     * starts to read the mail data; it may wrap the mail data input stream into
     * another stream, but it must not read the data itself.
     * 
     * It corresponds to the {@link FilterSession#dataStream()} function of
     * stateful filters, see that for more information.
     * 
     * @param transaction
     *            The mail transaction which is in progress.
     * 
     * @see FilterSession#dataStream()
     */
    protected void dataStream(MailTransaction transaction) {
        // do nothing
    }

    /**
     * It is called after the DATA command is accepted, and the mail data is
     * received; it may check the mail data or deliver the mail.
     * 
     * It corresponds to the {@link FilterSession#data()} function of stateful
     * filters, see that for more information.
     * 
     * @param transaction
     *            The mail transaction which is in progress.
     * @throws RejectExceptionExt
     *             if the mail data must be rejected, for example the content is
     *             SPAM.
     * @throws MaildataFileReadException
     *             if there was an IOExcepton while the mail data stream was
     *             read. This is a RuntimeException, but the higher level code
     *             understands that it indicates a local system error and not a
     *             bug in the code.
     * 
     * @see FilterSession#data()
     */
    protected void data(MailTransaction transaction) throws RejectExceptionExt,
            MaildataFileReadException {
        // do nothing
    }

    /**
     * It is called after the mail transaction is completed or aborted; it may
     * clean up the resources it may stored in the transaction object.
     * 
     * It corresponds to the {@link FilterSession#done()} function of stateful
     * filters, see that for more information.
     * 
     * @param transaction
     *            The mail transaction which is in progress.
     * 
     * @see FilterSession#done()
     */
    protected void done(MailTransaction transaction) {
        // do nothing
    }

    private class Session extends FilterSession {
        @Override
        public void begin() {
            StatelessFilter.this.begin(transaction);
            super.begin();
        }

        @Override
        public void from() throws RejectExceptionExt {
            StatelessFilter.this.from(transaction);
            super.from();
        }

        @Override
        public RecipientVerificationResult verifyRecipient(
                RecipientContext recipientContext) throws RejectExceptionExt {
            RecipientVerificationResult result =
                    StatelessFilter.this.verifyRecipient(transaction,
                            recipientContext);
            switch (result) {
            case ACCEPT:
                return RecipientVerificationResult.ACCEPT;
            case NEUTRAL:
                return super.verifyRecipient(recipientContext);
            default:
                throw new AssertionException();
            }
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            StatelessFilter.this.recipient(transaction, recipientContext);
            super.recipient(recipientContext);
        }

        @Override
        public void dataStream() {
            StatelessFilter.this.dataStream(transaction);
            super.dataStream();
        }

        @Override
        public void data() throws RejectExceptionExt, MaildataFileReadException {
            StatelessFilter.this.data(transaction);
            super.data();
        }

        @Override
        public void done() {
            StatelessFilter.this.done(transaction);
            super.done();
        }

    }
}
