package mireka.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import mireka.maildata.Maildata;
import mireka.maildata.io.TmpMaildataFile;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MimeMessageConverter {
    private Logger logger = LoggerFactory.getLogger(MimeMessageConverter.class);

    public Maildata createMailDataInSmtpSession(MimeMessage mimeMessage)
            throws RejectExceptionExt {
        TmpMaildataFile maildataFile = new TmpMaildataFile();
        try (OutputStream out = maildataFile.deferredFile.getOutputStream()) {
            mimeMessage.writeTo(out);
        } catch (IOException e) {
            logger.error("Cannot write MimeMessage", e);
            maildataFile.close();
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        } catch (MessagingException e) {
            logger.error("Cannot write MimeMessage", e);
            maildataFile.close();
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
        return new Maildata(maildataFile);
    }

}
