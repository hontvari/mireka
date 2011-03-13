package mireka.destination;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.ParseException;

import mireka.ConfigurationException;
import mireka.address.MailAddressFactory;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;
import mireka.util.MimeMessageConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the mail sent to the recipient and forwards the result. Note: this
 * minimal implementation can only send a new mail with the specified subject
 * and an empty body.
 */
public class TransformDestination implements MailDestination {
    private final Logger logger = LoggerFactory
            .getLogger(TransformDestination.class);
    private String subject;
    private String recipient;
    private String reversePath;
    private String from;
    private Transmitter transmitter;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        MimeMessage mimeMessage = createMimeMessage();
        sendMail(mail, mimeMessage);
    }

    private MimeMessage createMimeMessage() throws RejectExceptionExt {
        try {
            Properties props = new Properties();
            Session session = Session.getInstance(props);
            MimeMessage message = new MimeMessage(session);
            try {
                message.setFrom(new InternetAddress(from));
                message.setRecipient(RecipientType.TO, new InternetAddress(
                        recipient));
            } catch (AddressException e) {
                throw new ConfigurationException(e);
            }
            message.setSubject(subject, "UTF-8");
            message.setText("");
            return message;
        } catch (MessagingException e) {
            logger.error("Cannot create a mail list MimeMessage", e);
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
    }

    private void sendMail(Mail srcMail, MimeMessage mimeMessage)
            throws RejectExceptionExt {
        Mail mail = new Mail();
        mail.from = reversePath;
        try {
            mail.recipients.add(new MailAddressFactory()
                    .createRecipient(recipient));
        } catch (ParseException e) {
            throw new ConfigurationException(e);
        }

        mail.mailData =
                new MimeMessageConverter()
                        .createMailDataInSmtpSession(mimeMessage);
        try {
            mail.arrivalDate = srcMail.arrivalDate;
            mail.scheduleDate = mail.arrivalDate; // try to preserve order
            transmitter.transmit(mail);
            logger.debug("Transformed mail was submitted to transmitter: {}",
                    mail);
        } catch (LocalMailSystemException e) {
            logger.error("Cannot transmit mail", e);
            throw new RejectExceptionExt(e.errorStatus());
        } finally {
            mail.mailData.dispose();
        }
    }

    @Override
    public String toString() {
        return "TransformDestination [reversePath=" + reversePath
                + ", recipient=" + recipient + "]";
    }

    /**
     * @category GETSET
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @category GETSET
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @category GETSET
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * @category GETSET
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * @category GETSET
     */
    public String getReversePath() {
        return reversePath;
    }

    /**
     * @category GETSET
     */
    public void setReversePath(String reversePath) {
        this.reversePath = reversePath;
    }

    /**
     * @category GETSET
     */
    public String getFrom() {
        return from;
    }

    /**
     * @category GETSET
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @category GETSET
     */
    public Transmitter getTransmitter() {
        return transmitter;
    }

    /**
     * @category GETSET
     */
    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

}
