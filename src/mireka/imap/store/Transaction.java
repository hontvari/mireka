package mireka.imap.store;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.destination.CombinedMailDestination;
import mireka.imap.UnavailableException;

/**
 * Coordinates transactional services. POP3 could also be involved when used with the
 * {@link CombinedMailDestination}.
 */
public class Transaction {
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private static final ThreadLocal<Transaction> instance = new ThreadLocal<>();

    private StoreTransaction storeTransaction;
    /**
     * These are running after the {@link #storeTransaction} commit. They put mailbox state change
     * notifications onto the queues of sessions which have opened the mailbox.
     */
    private List<Runnable> afterCommitJobs = new ArrayList<>();

    public static Transaction init() {
        logger.trace("init");
        Transaction t = instance.get();
        if (t != null)
            throw new IllegalStateException();
        t = new Transaction();
        instance.set(t);
        return t;
    }

    /**
     * returns the ThreadLocal instance.
     */
    public static @Nonnull Transaction get() {
        Transaction t = instance.get();
        if (t == null)
            throw new IllegalStateException();
        return t;
    }

    public static void commit() throws UnavailableException {
        logger.trace("commit");
        Transaction t = get();
        if (t.storeTransaction != null)
            t.storeTransaction.commit();
        for (Runnable r : t.afterCommitJobs)
            r.run();
        t.afterCommitJobs.clear();
    }

    public static void setStoreTransaction(StoreTransaction st) {
        Transaction t = get();
        if (t.storeTransaction != null)
            throw new IllegalStateException();
        t.storeTransaction = st;
    }

    public static void runAfterCommit(Runnable u) {
        Transaction t = instance.get();
        if (t == null)
            throw new IllegalStateException();
        t.afterCommitJobs.add(u);
    }

    public static void cleanup() {
        logger.trace("cleanup");
        Transaction t = instance.get();
        instance.remove();
        if (t == null)
            return;
        if (t.storeTransaction != null)
            t.storeTransaction.close();
    }
}
