package mireka.list;

import java.util.regex.Pattern;

import javax.mail.MessagingException;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;

/**
 * SubjectRegexpValidator accepts a mail if its subject matches the specified
 * regular expression pattern.
 */
public class SubjectRegexpValidator implements MailValidator {
    private Pattern pattern;

    @Override
    public boolean shouldBeAccepted(ParsedMail mail) throws RejectExceptionExt {
        try {
            String subject = mail.getMimeMessage().getSubject();
            if (subject == null)
                subject = "";
            return pattern.matcher(subject).matches();
        } catch (MessagingException e) {
            throw new RejectExceptionExt(EnhancedStatus.BAD_MESSAGE_BODY);
        }
    }

    /**
     * @category GETSET
     */
    public String getPattern() {
        return pattern.toString();
    }

    /**
     * @category GETSET
     */
    public void setPattern(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

}
