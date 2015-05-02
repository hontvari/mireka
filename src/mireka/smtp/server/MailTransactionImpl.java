package mireka.smtp.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mireka.address.Recipient;
import mireka.destination.Destination;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.maildata.Maildata;

import org.subethamail.smtp.MessageContext;

public class MailTransactionImpl implements MailTransaction {
    private final Map<String, Object> attributes =
            new HashMap<String, Object>();

    public final MessageContext messageContext;

    public String from;

    public List<RecipientContext> recipientContexts =
            new ArrayList<RecipientContext>();

    private Maildata data;

    /**
     * Contains only accepted recipients.
     */
    private final Map<Recipient, Destination> recipientDestinationMap =
            new HashMap<Recipient, Destination>();

    public MailTransactionImpl(MessageContext messageContext) {
        super();
        this.messageContext = messageContext;
    }

    /**
     * it resets the stream if necessary before returning it
     */
    @Override
    public Maildata getData() {
        return data;
    }

    public void setData(Maildata data) {
        this.data = data;
    }

    @Override
    public void replaceData(Maildata maildata) {
        if (this.data != null && this.data != maildata)
            this.data.close();
        this.data = maildata;
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
    public List<RecipientContext> getAcceptedRecipientContexts() {
        return recipientContexts;
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

    public void addDestinationForRecipient(Recipient recipient,
            Destination destination) {
        recipientDestinationMap.put(recipient, destination);
    }

}
