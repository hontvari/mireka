package mireka.transmission.queue;

import java.util.concurrent.atomic.AtomicInteger;

public class TransmitterSummary implements TransmitterSummaryMBean {

    public final AtomicInteger mailTransactions = new AtomicInteger();
    public final AtomicInteger successfulMailTransactions = new AtomicInteger();
    public final AtomicInteger failures = new AtomicInteger();
    public final AtomicInteger permanentFailures = new AtomicInteger();
    public final AtomicInteger transientFailures = new AtomicInteger();
    public final AtomicInteger partialFailures = new AtomicInteger();
    public volatile Throwable lastFailure;
    public final AtomicInteger errors = new AtomicInteger();
    public volatile Throwable lastError;

    @Override
    public int getMailTransactions() {
        return mailTransactions.get();
    }

    @Override
    public int getSuccessfulMailTransactions() {
        return successfulMailTransactions.get();
    }

    @Override
    public int getFailures() {
        return failures.get();
    }

    @Override
    public int getPartialFailures() {
        return partialFailures.get();
    }

    @Override
    public int getPermanentFailures() {
        return permanentFailures.get();
    }

    @Override
    public int getTransientFailures() {
        return transientFailures.get();
    }

    @Override
    public double getFailuresPercentage() {
        int mailTransactionsInt = mailTransactions.get();
        if (mailTransactionsInt == 0)
            return 0;
        return Math.round(1000.0 * failures.get() / mailTransactionsInt) / 10;
    }

    @Override
    public double getPermanentFailuresPercentage() {
        int mailTransactionsInt = mailTransactions.get();
        if (mailTransactionsInt == 0)
            return 0;
        return Math.round(1000.0 * permanentFailures.get()
                / mailTransactionsInt) / 10;
    }

    @Override
    public double getTransientFailuresPercentage() {
        int mailTransactionsInt = mailTransactions.get();
        if (mailTransactionsInt == 0)
            return 0;
        return Math.round(1000.0 * transientFailures.get()
                / mailTransactionsInt) / 10;
    }

    @Override
    public Throwable getLastFailure() {
        return lastFailure;
    }

    @Override
    public int getErrors() {
        return errors.get();
    }

    @Override
    public Throwable getLastError() {
        return lastError;
    }
}
