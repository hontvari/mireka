package mireka.list;

import mireka.smtp.RejectExceptionExt;

/**
 * MailValidator validates a mail based on its content, in contrast to
 * validating based on the mail envelope.
 */
public interface MailValidator {
    boolean shouldBeAccepted(ParsedMail mail) throws RejectExceptionExt;
}
