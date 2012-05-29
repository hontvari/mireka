package mireka.transmission.queue;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.management.JMException;
import javax.management.ObjectName;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;

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
                Metrics.newMeter(TransmitterSummary.class, "mailTransactions",
                        name, "transactions", TimeUnit.MINUTES);
        successfulMailTransactions =
                Metrics.newMeter(TransmitterSummary.class,
                        "successfulMailTransactions", name, "transactions",
                        TimeUnit.MINUTES);
        failures =
                Metrics.newMeter(TransmitterSummary.class, "failures", name,
                        "transactions", TimeUnit.MINUTES);
        permanentFailures =
                Metrics.newMeter(TransmitterSummary.class, "permanentFailures",
                        name, "transactions", TimeUnit.MINUTES);
        transientFailures =
                Metrics.newMeter(TransmitterSummary.class, "transientFailures",
                        name, "transactions", TimeUnit.MINUTES);
        partialFailures =
                Metrics.newMeter(TransmitterSummary.class, "partialFailures",
                        name, "transactions", TimeUnit.MINUTES);
        errors =
                Metrics.newMeter(TransmitterSummary.class, "errors", name,
                        "transactions", TimeUnit.MINUTES);
    }

    /**
     * @category GETSET
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getMailTransactions() {
        return mailTransactions.count();
    }

    @Override
    public long getSuccessfulMailTransactions() {
        return successfulMailTransactions.count();
    }

    @Override
    public long getFailures() {
        return failures.count();
    }

    @Override
    public long getPartialFailures() {
        return partialFailures.count();
    }

    @Override
    public long getPermanentFailures() {
        return permanentFailures.count();
    }

    @Override
    public long getTransientFailures() {
        return transientFailures.count();
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
    public long getErrors() {
        return errors.count();
    }

    @Override
    public String getLastError() {
        return lastError;
    }
}
