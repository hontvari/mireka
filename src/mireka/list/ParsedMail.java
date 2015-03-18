package mireka.list;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParsedMail encompasses both the original {@link Mail} object and the mail
 * body lazily parsed into a {@link MimeMessage} format.
 */
public class ParsedMail {
    private Logger logger = LoggerFactory.getLogger(ParsedMail.class);
    private final Mail mail;
    private MimeMessage mimeMessage;

    public ParsedMail(Mail mail) {
        this.mail = mail;
    }

    public MimeMessage getMimeMessage() throws RejectExceptionExt {
        if (mimeMessage == null) {
            mimeMessage = parseMessage();
        }
        return mimeMessage;
    }

    private MimeMessage parseMessage() throws RejectExceptionExt {
        Properties properties = new Properties();
        properties.setProperty("mail.mime.charset", "UTF-8");
        properties.setProperty("mail.mime.decodefilename", "true");
        properties.setProperty("mail.mime.encodefilename", "true");
        properties.setProperty("mail.mime.decodeparameters", "true");
        properties.setProperty("mail.mime.encodeparameters", "true");
        Session session = Session.getInstance(properties);
        InputStream in;
        try {
            in = getMail().mailData.getInputStream();
        } catch (IOException e) {
            logger.error("Cannot open mail data input stream", e);
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
        try {
            return new MimeMessage(session, in);
        } catch (MessagingException e) {
            logger.debug("Cannot parse MimeMessage", e);
            throw new RejectExceptionExt(EnhancedStatus.BAD_MESSAGE_BODY);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.warn("Cannot close mail data input stream", e);
            }
        }
    }

    /**
     * @x.category GETSET
     */
    public Mail getMail() {
        return mail;
    }
}
