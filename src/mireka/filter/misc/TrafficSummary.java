package mireka.filter.misc;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class TrafficSummary implements TrafficSummaryMBean {
    public final Date since = new Date();
    public final AtomicInteger mailTransactions = new AtomicInteger();
    public final AtomicInteger rcptCommands = new AtomicInteger();
    public final AtomicInteger dataCommands = new AtomicInteger();
    public final AtomicInteger acceptedMessages = new AtomicInteger();
    public final AtomicInteger messageRecipients = new AtomicInteger();
    private ObjectName objectName;

    @PostConstruct
    public void register() {
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(this,
                    objectName);
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @category GETSET
     */
    public void setObjectName(String objectName) {
        try {
            this.objectName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

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
