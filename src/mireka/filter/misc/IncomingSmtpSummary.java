package mireka.filter.misc;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;

/**
 * IncomingSmtpSummary holds and publishes metrics about the traffic on a single
 * SMTP server port of Mireka.
 */
public class IncomingSmtpSummary {
    private String name;
    public Meter mailTransactions;
    public Meter rcptCommands;
    public Meter dataCommands;
    public Meter acceptedMessages;
    public Meter messageRecipients;

    @PostConstruct
    public void register() {
        mailTransactions =
                Metrics.newMeter(metricName("mailTransactions"),
                        "transactions", TimeUnit.MINUTES);
        rcptCommands =
                Metrics.newMeter(metricName("rcptCommands"), "commands",
                        TimeUnit.MINUTES);
        dataCommands =
                Metrics.newMeter(metricName("dataCommands"), "commands",
                        TimeUnit.MINUTES);
        acceptedMessages =
                Metrics.newMeter(metricName("acceptedMessages"), "messages",
                        TimeUnit.MINUTES);
        messageRecipients =
                Metrics.newMeter(metricName("messageRecipients"), "recipients",
                        TimeUnit.MINUTES);
    }

    private MetricName metricName(String metricName) {
        return new MetricName("mireka", "IncomingSmtpTraffic", metricName,
                this.name);
    }

    /**
     * @x.category GETSET
     */
    public void setName(String name) {
        this.name = name;
    }
}
