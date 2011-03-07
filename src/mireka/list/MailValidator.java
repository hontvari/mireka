package mireka.list;

import mireka.smtp.RejectExceptionExt;

public interface MailValidator {
    boolean shouldBeAccepted(ParsedMail mail) throws RejectExceptionExt;
}
