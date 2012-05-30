package mireka.transmission.queue;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.management.JMException;
import javax.management.ObjectName;

import mireka.transmission.queuing.QueuingTransmitter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;

/**
 * TransmitterSummary holds and publishes statistics data about the mail
 * transactions of a {@link QueuingTransmitter}.
 */
public class TransmitterSummary implements TransmitterSummaryMBean {
    private String name;
    public Meter mailTransactions;
    public Meter successfulMailTransactions;
    public Meter failures;
    public Meter permanentFailures;
    public Meter transientFailures;
    public Meter partialFailures;
    public volatile String lastFailure;
    public Meter errors;
    public volatile String lastError;

    @PostConstruct
    public void register() {
        try {
            ObjectName objectName =
                    new ObjectName("mireka:type=TransmitterTraffic,name="
                            + name);
            ManagementFactory.getPlatformMBeanServer().registerMBean(this,
                    objectName);
        } catch (JMException e) {
            throw new RuntimeException(e);
        }

        mailTransactions =
                Metrics.newMeter(metricName("mailTransactions"),
                        "transactions", TimeUnit.MINUTES);
        successfulMailTransactions =
                Metrics.newMeter(metricName("successfulMailTransactions"),
                        "transactions", TimeUnit.MINUTES);
        failures =
                Metrics.newMeter(metricName("failures"), "transactions",
                        TimeUnit.MINUTES);
        permanentFailures =
                Metrics.newMeter(metricName("permanentFailures"),
                        "transactions", TimeUnit.MINUTES);
        transientFailures =
                Metrics.newMeter(metricName("transientFailures"),
                        "transactions", TimeUnit.MINUTES);
        partialFailures =
                Metrics.newMeter(metricName("partialFailures"), "transactions",
                        TimeUnit.MINUTES);
        errors =
                Metrics.newMeter(metricName("errors"), "transactions",
                        TimeUnit.MINUTES);
    }

    private MetricName metricName(String metricName) {
        return new MetricName("mireka", "TransmitterTraffic", metricName,
                this.name);
    }

    /**
     * @category GETSET
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public double getFailuresPercentage() {
        double total = mailTransactions.fifteenMinuteRate();
        if (total == 0)
            return 0;
        return Math.round(1000.0 * failures.fifteenMinuteRate() / total) / 10;
    }

    @Override
    public double getPermanentFailuresPercentage() {
        double total = mailTransactions.fifteenMinuteRate();
        if (total == 0)
            return 0;
        return Math.round(1000.0 * permanentFailures.fifteenMinuteRate()
                / total) / 10;
    }

    @Override
    public double getTransientFailuresPercentage() {
        double total = mailTransactions.fifteenMinuteRate();
        if (total == 0)
            return 0;
        return Math.round(1000.0 * transientFailures.fifteenMinuteRate()
                / total) / 10;
    }

    @Override
    public String getLastFailure() {
        return lastFailure;
    }

    @Override
    public String getLastError() {
        return lastError;
    }
}
