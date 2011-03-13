package mireka.util;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import mireka.MailData;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.server.DeferredFileMailData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.DeferredFileOutputStream;

public class MimeMessageConverter {
    private Logger logger = LoggerFactory.getLogger(MimeMessageConverter.class);

    public MailData createMailDataInSmtpSession(MimeMessage mimeMessage)
            throws RejectExceptionExt {
        DeferredFileOutputStream deferredFileOutputStream =
                new DeferredFileOutputStream(32768);
        try {
            mimeMessage.writeTo(deferredFileOutputStream);
        } catch (IOException e) {
            logger.error("Cannot write MimeMessage", e);
            try {
                deferredFileOutputStream.close();
            } catch (IOException e1) {
                logger.warn("Cannot close deferredFileOutputStream", e1);
            }
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        } catch (MessagingException e) {
            logger.error("Cannot write MimeMessage", e);
            try {
                deferredFileOutputStream.close();
            } catch (IOException e1) {
                logger.warn("Cannot close deferredFileOutputStream", e1);
            }
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
        return new DeferredFileMailData(deferredFileOutputStream);
    }

}
