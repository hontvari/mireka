package mireka.filter.misc;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class TrafficSummary implements TrafficSummaryMBean {
    public final Date since = new Date();
    public final AtomicInteger mailTransactions = new AtomicInteger();
    public final AtomicInteger rcptCommands = new AtomicInteger();
    public final AtomicInteger dataCommands = new AtomicInteger();
    public final AtomicInteger acceptedMessages = new AtomicInteger();
    public final AtomicInteger messageRecipients = new AtomicInteger();

    @Override
    public Date getSince() {
        return since;
    }

    @Override
    public int getMailTransactions() {
        return mailTransactions.get();
    }

    @Override
    public int getRcptCommands() {
        return rcptCommands.get();
    }

    @Override
    public int getDataCommands() {
        return dataCommands.get();
    }

    @Override
    public int getAcceptedMessages() {
        return acceptedMessages.get();
    }

    @Override
    public int getMessageRecipients() {
        return messageRecipients.get();
    }
}
