package mireka.transmission.queuing;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.ThreadSafe;

/**
 * produces log identifiers which can be printed into the final-log-id field of
 * a mail delivery status report.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc3464#section-2.3.8"> rfc3464 -
 *      2.3.8 final-log-id field</a>
 */
@ThreadSafe
public class LogIdFactory {
    private final AtomicLong serial =
            new AtomicLong(System.currentTimeMillis());

    public String next() {
        return "L" + serial.getAndIncrement();
    }
}
