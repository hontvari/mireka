package mireka.filter;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mireka.address.Recipient;
import mireka.address.ReversePath;
import mireka.destination.Destination;
import mireka.filter.spf.SpfChecker;
import mireka.maildata.Maildata;
import mireka.smtp.server.SmtpDataReadException;

import org.apache.james.jspf.executor.SPFResult;
import org.subethamail.smtp.MessageContext;

/**
 * MailTransaction gathers data during an SMTP mail transaction, from the MAIL
 * command to the acceptance of the mail data following the DATA command, and it
 * makes them available as the transaction - which consists of several steps -
 * progresses. Filters can examine and change the data in each step.
 */
public class MailTransaction {

    /**
     * SubEthaSMTP message context object, which may provide some additional
     * information about this mail transaction.
     */
    public final MessageContext messageContext;

    /**
     * The reverse-path supplied in the SMTP MAIL FROM command. Null if the
     * transaction is not yet progressed to this state.
     */
    public ReversePath reversePath;

    /**
     * List of accepted recipients. Recipients are specified by the SMTP RCPT
     * command. A RecipientContext will be added to this list only after both
     * the {@link FilterSession#verifyRecipient(RecipientContext)} and
     * {@link FilterSession#recipient(RecipientContext)} chains returned without
     * rejecting the recipient.
     */
    public List<RecipientContext> recipientContexts =
            new ArrayList<RecipientContext>();

    /**
     * The input stream which can be read to get the mail data. It is the data
     * which follows the acknowledgement of the SMTP DATA command. It is set
     * immediately before the {@link FilterSession#dataStream()} function is
     * called on filters. Those filters may wrap the original stream with
     * another stream, which will prepend data to the stream or check the
     * content of the stream as it will be read.
     * 
     * A wrapper stream may throw TooMuchDataException is the mail data is
     * longer than allowed. This exception is handled specially by the SMTP
     * code, the specific SMTP error will be returned.
     * 
     * The original input stream installed by the SMTP handling code throws a
     * specific IOException, {@link SmtpDataReadException} exception, on an IO
     * error, which in this case means an error in the TCP connection from the
     * client. The SMTP code specifically checks for this exception to determine
     * if the exception is related to the TCP connection from the client.
     * Therefore the wrapping stream class must not swallow these exceptions and
     * it must not wrap them into another exception.
     * 
     * At the end of the transaction the input stream will be flushed, but it
     * will not be closed, because it is only a wrapper around the TCP
     * connection to the client.
     * 
     * @see org.subethamail.smtp.TooMuchDataException
     */
    public InputStream dataStream;

    /**
     * The mail data which is received after the accepted SMTP data command, in
     * an on-demand parsed, Document Object Model like form.
     * 
     * This field is set before the call to {@link FilterSession#data()}.
     * 
     * After the <code>data</code> and <code>dataRecipient</code> chain the SMTP
     * handler code closes both the original <code>Maildata</code> object and
     * the final Maildata object (if the original is replaced by a filter).
     * However if a filter replaces this data object than it must close the
     * original. Therefore a filter should call {@link #replaceData(Maildata)}
     * to change the value of this field.
     */
    public Maildata data;

    /**
     * The cached result of the SPF check. Null if no SPF check has been
     * completed. This field is maintained by the {@link SpfChecker} class.
     */
    public SPFResult spfResult;

    /**
     * Contains only accepted recipients.
     */
    private final Map<Recipient, Destination> recipientDestinationMap =
            new HashMap<Recipient, Destination>();

    /**
     * A map which can store unspecified data, which can be used to pass ad-hoc
     * data between two custom filters. Built-in filters use type safe fields in
     * this object, not this facility. Attribute names should be unique, they
     * should be prefixed with a reversed domain, like Java packages. For
     * example "org.example.mailfilter.blacklist". Although it shouldn't be
     * used, the <code>mireka</code> prefix is reserved for built-in code.
     */
    public final Map<String, Object> attributes = new HashMap<>();

    public MailTransaction(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    /**
     * It closes the current Maildata instance (if it is different from the
     * specified), and replaces it with the specified one.
     * 
     * Filters shouldn't call this method, instead they should pass a new
     * Maildata to the next element in the chain.
     */
    public void replaceData(Maildata maildata) {
        if (this.data != null && this.data != maildata)
            this.data.close();
        this.data = maildata;
    }

    /**
     * @x.category GETSET
     */
    public MessageContext getMessageContext() {
        return messageContext;
    }

    /**
     * convenience function, a better place for this would be
     * {@link MessageContext}
     */
    public InetAddress getRemoteInetAddress() {
        return ((InetSocketAddress) messageContext.getRemoteAddress())
                .getAddress();
    }

    public void addDestinationForRecipient(Recipient recipient,
            Destination destination) {
        recipientDestinationMap.put(recipient, destination);
    }

}
