package mireka.list;

import java.util.regex.Pattern;

import javax.mail.MessagingException;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;

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
    public boolean shouldBeAccepted(ParsedMail mail) throws RejectExceptionExt {
        try {
            String subject = mail.getMimeMessage().getSubject();
            if (subject == null)
                subject = "";
            boolean result = pattern.matcher(subject).matches();
            if (result)
                logger.debug("Mail accepted, subject matches "
                        + pattern.toString());
            return result;
        } catch (MessagingException e) {
            throw new RejectExceptionExt(EnhancedStatus.BAD_MESSAGE_BODY);
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
