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
    private Meter mailTransactions;
    private Meter successfulMailTransactions;
    private Meter failures;
    private Meter permanentFailures;
    private Meter transientFailures;
    private Meter partialFailures;
    public volatile String lastFailure;
    /**
     * Meter which counts unexpected Java exceptions occurred during mail
     * transmission that were not handled by sending an SMTP error reply and
     * that are not expected, usual connection problems.
     */
    private Meter errors;
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

    private MetricName metricName(String name) {
        return new MetricName("mireka", "TransmitterTraffic", name, this.name);
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

    public Meter mailTransactionsMeter() {
        return mailTransactions;
    }

    public Meter successfulMailTransactionsMeter() {
        return successfulMailTransactions;
    }

    public Meter failuresMeter() {
        return failures;
    }

    public Meter permanentFailuresMeter() {
        return permanentFailures;
    }

    public Meter transientFailuresMeter() {
        return transientFailures;
    }

    public Meter partialFailuresMeter() {
        return partialFailures;
    }

    public Meter errorsMeter() {
        return errors;
    }
}
