package mireka.filter;

import java.net.InetAddress;
import java.util.List;

import mireka.mailaddress.Recipient;

import org.subethamail.smtp.MessageContext;

public interface MailTransaction {

    /**
     * null if not yet received, empty string in case of a null reverse path
     */
    String getFrom();

    /**
     * accepted recipients
     */
    List<Recipient> getRecipients();

    /**
     * null if data is not received yet
     */
    MailData getData();

    /**
     * filters shouldn't call this method, instead they should simply wrap the
     * {@link MailData} object they receive and pass it to the next element in
     * the chain.
     */
    void replaceData(MailData mailData);

    /**
     * @category GETSET
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