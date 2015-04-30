package mireka.list;

import java.io.IOException;
import java.util.regex.Pattern;

import mireka.maildata.WritableMaildata;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubjectRegexpValidator accepts a mail if its subject matches the specified
 * regular expression pattern.
 */
public class SubjectRegexpValidator implements MailValidator {
    private final Logger logger = LoggerFactory
            .getLogger(SubjectRegexpValidator.class);
    private Pattern pattern;

    @Override
    public boolean shouldBeAccepted(Mail mail) throws RejectExceptionExt {
        try {
            String subject =
                    new WritableMaildata(mail.mailData).header().getSubject();

            if (subject == null)
                subject = "";
            boolean result = pattern.matcher(subject).matches();
            if (result)
                logger.debug("Mail accepted, subject matches "
                        + pattern.toString());
            return result;
        } catch (IOException e) {
            // TODO this will not be needed after introduction of
            // VirtFileReadException
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
    }

    /**
     * @x.category GETSET
     */
    public String getPattern() {
        return pattern.toString();
    }

    /**
     * @x.category GETSET
     */
    public void setPattern(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * @x.category GETSET
     */
    public void setValue(String pattern) {
        setPattern(pattern);
    }
}
