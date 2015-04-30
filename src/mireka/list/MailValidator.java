package mireka.list;

import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

/**
 * MailValidator validates a mail based on its content, in contrast to
 * validating based on the mail envelope.
 */
public interface MailValidator {
    boolean shouldBeAccepted(Mail mail) throws RejectExceptionExt;
}
