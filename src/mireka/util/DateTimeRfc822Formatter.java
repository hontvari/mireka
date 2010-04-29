package mireka.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.concurrent.Immutable;

/**
 * @see <a href="http://tools.ietf.org/html/rfc5322#section-3.3">rfc5322 - 3.3.
 *      Date and Time Specification</a>
 */
@Immutable
public class DateTimeRfc822Formatter {
    public String format(Date date) {
        DateFormat dateFormat =
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z (z)",
                        Locale.US);
        return dateFormat.format(date);
    }
}
