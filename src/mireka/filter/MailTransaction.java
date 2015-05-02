package mireka.filter;

import java.net.InetAddress;
import java.util.List;

import mireka.maildata.Maildata;

import org.subethamail.smtp.MessageContext;

/**
 * It follows an incoming mail transaction, it makes data available as the
 * transaction - which consists of several steps - progresses.
 */
public interface MailTransaction {

    /**
     * Returns the accepted reverse-path supplied in the MAIL SMTP command.
     * 
     * @return null if is is not yet received, or if it was rejected, empty
     *         string in case of a null reverse-path
     */
    String getFrom();

    /**
     * accepted recipients
     */
    List<RecipientContext> getAcceptedRecipientContexts();

    /**
     * null if data is not received yet
     */
    Maildata getData();

    /**
     * It closes the current Maildata instance (if it is different from the
     * specified), and replaces it with the specified one.
     * 
     * Filters shouldn't call this method, instead they should pass a new
     * Maildata to the next element in the chain.
     */
    void replaceData(Maildata maildata);

    /**
     * @x.category GETSET
     */
    MessageContext getMessageContext();

    /**
     * convenience function, a better place for this would be
     * {@link MessageContext}
     */
    InetAddress getRemoteInetAddress();

    Object getAttribute(String name);

    void setAttribute(String name, Object value);
}