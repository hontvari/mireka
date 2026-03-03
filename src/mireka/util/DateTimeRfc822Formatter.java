package mireka.util;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.annotation.concurrent.Immutable;

/**
 * @see <a href="http://tools.ietf.org/html/rfc5322#section-3.3">rfc5322 - 3.3.
 *      Date and Time Specification</a>
 */
@Immutable
public class DateTimeRfc822Formatter {
    public String format(Instant date) {
        DateTimeFormatter dateFormat = DateTimeFormatter
                .ofPattern("EEE, dd MMM yyyy HH:mm:ss Z (z)", Locale.US);
        return dateFormat.format(date);
    }
}
