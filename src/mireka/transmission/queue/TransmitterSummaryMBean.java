package mireka.transmission.queue;

public interface TransmitterSummaryMBean {
    long getMailTransactions();

    long getSuccessfulMailTransactions();

    long getFailures();

    long getPartialFailures();

    long getPermanentFailures();

    long getTransientFailures();

    double getFailuresPercentage();

    double getPermanentFailuresPercentage();

    double getTransientFailuresPercentage();

    String getLastFailure();

    long getErrors();

    String getLastError();
}
