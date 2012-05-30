package mireka.transmission.queue;

/**
 * JMX interface definition for {@link TransmitterSummary}.
 */
public interface TransmitterSummaryMBean {

    double getFailuresPercentage();

    double getPermanentFailuresPercentage();

    double getTransientFailuresPercentage();

    String getLastFailure();

    String getLastError();
}
