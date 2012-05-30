package mireka.startup;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.yammer.metrics.Metrics;

/**
 * GraphiteReporter sends metrics data maintained by the Metrics library to a
 * Graphite server for graphical viewing.
 * 
 * @see <a href="http://metrics.codahale.com/">Metrics library</a>
 * @see <a href="http://graphite.wikidot.com/">Graphite</a>
 */
public class GraphiteReporter {
    private String host;
    private int port = 2003;
    private String prefix = "mail";
    private long period = 10;
    private TimeUnit periodUnit = TimeUnit.SECONDS;

    private com.yammer.metrics.reporting.GraphiteReporter reporter;

    @PostConstruct
    public void start() {
        try {
            reporter =
                    new com.yammer.metrics.reporting.GraphiteReporter(
                            Metrics.defaultRegistry(), host, port, prefix);
            reporter.start(period, periodUnit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop() {
        reporter.shutdown();
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void setPeriodUnit(TimeUnit periodUnit) {
        this.periodUnit = periodUnit;
    }
}
