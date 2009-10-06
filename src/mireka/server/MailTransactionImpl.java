package mireka.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mireka.filter.MailData;
import mireka.filter.MailTransaction;
import mireka.mailaddress.Recipient;

import org.subethamail.smtp.MessageContext;

public class MailTransactionImpl implements MailTransaction {
    private final Map<String, Object> attributes =
            new HashMap<String, Object>();

    public final MessageContext messageContext;

    public String from;

    public List<Recipient> recipients = new ArrayList<Recipient>();

    private MailData data;

    public MailTransactionImpl(MessageContext messageContext) {
        super();
        this.messageContext = messageContext;
    }

    /**
     * it resets the stream if necessary before returning it
     */
    @Override
    public MailData getData() {
        return data;
    }

    public void setData(MailData data) {
        this.data = data;
    }

    @Override
    public void replaceData(MailData mailData) {
        this.data = mailData;
    }

    @Override
    public MessageContext getMessageContext() {
        return messageContext;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public List<Recipient> getRecipients() {
        return recipients;
    }

    @Override
    public InetAddress getRemoteInetAddress() {
        return ((InetSocketAddress) messageContext.getRemoteAddress())
                .getAddress();
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}
