package mireka.filter;

import mireka.destination.UnknownRecipientDestination;
import mireka.maildata.io.MaildataFileReadException;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.server.FilterChainMessageHandler;

/**
 * A filter processes mails, its functions are called in the different phases of
 * the SMTP mail transaction. A mail transaction starts with the MAIL FROM
 * command and ends with QUIT or with the start of another mail transaction. It
 * can be aborted after any command.
 * 
 * Implementing classes are active parts of a filter chain. Their methods must
 * explicitly call the corresponding method of the next filter. In this way they
 * are able to get information about the results of methods of the following
 * filters. This design is similar to Servlet Filters.
 * 
 * @see <a
 *      href="http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/Filter.html">ServletFilter
 *      in the Servlet API</a>
 */
public abstract class FilterSession {
    /**
     * The next filter in the chain.
     */
    protected FilterSession nextLink;
    /**
     * The mail transaction which this session is associated with.
     */
    protected MailTransaction transaction;

    /**
     * Stores the reference to the next filter in the chain.
     */
    public final void setNextLink(FilterSession nextLink) {
        this.nextLink = nextLink;
    }

    /**
     * Stores the reference to the MailTransaction object.
     */
    public final void setMailTransaction(MailTransaction transaction) {
        this.transaction = transaction;
    }

    /**
     * Initialize the filter. It is called after the basic properties of the
     * object (<code>nextLink</code> and <code>transaction</code>) are set.
     * 
     * Because a mail transaction is started by the MAIL command, this function
     * is called after after the MAIL command is received but before the
     * <code>from</code> function is called. The difference between this
     * function and the {@link #from} function:
     * <ul>
     * <li>Their intended role is different. This function does initialization,
     * the <code>from</code> function should concentrate on the reverse path.
     * <li>This function does not call the same function of the next filter. It
     * will be called by the {@link FilterChainMessageHandler} calls begin on
     * each filter instance itself.
     * <li>This function will be called on all filter instances on the chain.
     * The <code>from</code> function of a filter may not be called if a
     * previous filter rejects the MAIL command.
     * </ul>
     * 
     * In contrast to the functions which follow the steps of the mail
     * transaction, this function must not call the same function on the next
     * filter.
     * 
     * This implementation does nothing.
     */
    public void begin() {
        // do nothing
    }

    /**
     * Processes the MAIL FROM command, maybe by rejecting it.
     * 
     * An overriding method can access the reverse path in the the transaction
     * object. Before this function is called the reverse path specified in the
     * command is saved in {@link MailTransaction#reversePath}.
     * 
     * This implementation does nothing except call the next filter in the
     * chain.
     * 
     * @throws RejectExceptionExt
     *             thrown if this filter wants to reject the mail. The mail
     *             transaction will not continue, but the client may start new
     *             one, by issuing another SMTP MAIL command.
     */
    public void from() throws RejectExceptionExt {
        nextLink.from();
    }

    /**
     * Decides if a recipient should be accepted. The decision can be a final
     * positive, a final negative, or a neutral answer. This function is not
     * called if a previous filter has already accepted or rejected the
     * recipient. In case of a neutral answer, other filters will decide. If all
     * filters return the neutral answer, then the recipient will be accepted if
     * a destination is assigned to it and the assigned destination is not an
     * {@link UnknownRecipientDestination}; otherwise it will be rejected as an
     * unknown user.
     * 
     * This function is called after an SMTP RCPT command is received from the
     * client.
     * 
     * This implementation does nothing except call the next filter in the
     * chain.
     * 
     * @throws RejectExceptionExt
     *             if the recipient is not valid and it must be rejected
     */
    public RecipientVerificationResult verifyRecipient(
            RecipientContext recipientContext) throws RejectExceptionExt {
        return nextLink.verifyRecipient(recipientContext);
    }

    /**
     * Processes an accepted recipient. In contrast to {@link #verifyRecipient},
     * all filters receive this function call (except if one of them rejects the
     * recipient).
     * 
     * It is only called if one of the filters accepted the recipient according
     * to the result of the <code>verifyRecipient</code> function.
     * 
     * It is called after the <code>verifyRecipient</code> chain is completed
     * for the specified recipient.
     * 
     * This implementation does nothing except call the next filter in the
     * chain.
     * 
     * @throws RejectExceptionExt
     *             if the recipient must be rejected. This shouldn't be a usual
     *             case, because the <code>verifyRecipient</code> chain already
     *             accepted the recipient. However an error condition can still
     *             arise in this step, even if the recipient address is valid.
     */
    public void recipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        nextLink.recipient(recipientContext);
    }

    /**
     * Optionally wraps the specified incoming mail data stream in another
     * InputStream, where the wrapping stream object can check or modify the
     * original stream as data is read from it. However this function must not
     * read from the chain.
     * 
     * It is called after the SMTP DATA command is acknowledged and the client
     * starts to send the mail data. The mail transaction has at least one
     * validated recipient at this point.
     * 
     * The wrapping stream must consider a few rules regarding its exception
     * handling, see {@link MailTransaction#dataStream} for more information.
     * 
     * This implementation does nothing except call the next filter in the
     * chain.
     * 
     * @see MailTransaction#dataStream
     */
    public void dataStream() {
        nextLink.dataStream();
    }

    /**
     * Checks, updates or consumes the complete received Mail Data. It is called
     * after #dataStream is called on all filters.
     * 
     * Before this method is called, mail data is stored in
     * {@link MailTransaction#data}.
     * 
     * This implementation does nothing except call the next filter in the
     * chain.
     * 
     * @throws RejectExceptionExt
     *             if the mail data must be rejected. For example if the mail
     *             data contains links to a known SPAM site.
     * @throws MaildataFileReadException
     *             if an IO error happens while reading of the mail data. The
     *             mail data at this phase of the mail transaction is already
     *             saved into local temporary storage and the data is coming
     *             from there, not directly from the client TCP connection. An
     *             IO error here is a very unlikely event, which affect many
     *             higher level classes, so this exception is unchecked, but the
     *             SMTP code understands that this exception indicates a local
     *             system error and not a program error.
     * 
     * 
     * @see MailTransaction#data
     */
    public void data() throws RejectExceptionExt, MaildataFileReadException {
        nextLink.data();
    }

    /**
     * Cleans up the filter. It is always called, even if some other filter
     * failed or no mail was delivered in this mail transaction.
     * 
     * This implementation does nothing.
     */
    public void done() {
        // nothing to do
    }

}