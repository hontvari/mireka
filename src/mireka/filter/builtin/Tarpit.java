package mireka.filter.builtin;

import java.util.Deque;
import java.util.LinkedList;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class Tarpit {
    private final long VALIDITY_DURATION = 30000;
    private final long WAIT_BY_MARK = 1000;
    private final long MAX_WAIT = 5000;
    /**
     * The count of expirations the system will store. Add one extra place so
     * that the wait duration does not drop temporarily when a mark expires
     * while otherwise a dictionary attack is ongoing.
     */
    private final int MAX_EXPIRATIONS =
            (int) Math.ceil((double) MAX_WAIT / WAIT_BY_MARK) + 1;
    /**
     * new entries must be added to the head
     */
    private Deque<Long> markExpirations = new LinkedList<Long>();

    public void addRejection() {
        removeExpiredMarks();
        Long expiration = System.currentTimeMillis() + VALIDITY_DURATION;
        markExpirations.addFirst(expiration);
        if (markExpirations.size() > MAX_EXPIRATIONS)
            markExpirations.removeLast();
    }

    private void removeExpiredMarks() {
        long now = System.currentTimeMillis();
        while (!markExpirations.isEmpty() && markExpirations.peekLast() <= now)
            markExpirations.removeLast();
    }

    public long waitDuration() {
        removeExpiredMarks();
        long waitDuration = markExpirations.size() * WAIT_BY_MARK;
        waitDuration = Math.min(MAX_WAIT, waitDuration);
        return waitDuration;
    }
}
