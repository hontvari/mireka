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
import mireka.maildata.MaildataFile;

import org.subethamail.smtp.MessageContext;

public class MailTransactionImpl implements MailTransaction {
    private final Map<String, Object> attributes =
            new HashMap<String, Object>();

    public final MessageContext messageContext;

    public String from;

    public List<RecipientContext> recipientContexts =
            new ArrayList<RecipientContext>();

    private MaildataFile data;

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
    public MaildataFile getData() {
        return data;
    }

    public void setData(MaildataFile data) {
        this.data = data;
    }

    @Override
    public void replaceData(MaildataFile mailData) {
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
