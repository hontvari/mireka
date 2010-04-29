package mireka.transmission.queue;

import mireka.transmission.Mail;

public interface MailProcessorFactory {
    MailProcessor create(Mail mail);

}
