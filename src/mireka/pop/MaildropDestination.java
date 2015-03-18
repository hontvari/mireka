package mireka.pop;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import mireka.destination.MailDestination;
import mireka.pop.store.Maildrop;
import mireka.pop.store.MaildropAppender;
import mireka.pop.store.MaildropRepository;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MaildropDestination puts the mail into the specified POP3 maildrop.
 */
public class MaildropDestination implements MailDestination {
    private final Logger logger = LoggerFactory
            .getLogger(MaildropDestination.class);
    private MaildropRepository maildropRepository;
    private String maildropName;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        Maildrop maildrop = maildropRepository.borrowMaildrop(maildropName);
        try {

            MaildropAppender appender;
            try {
                appender = maildrop.allocateAppender();
            } catch (LocalMailSystemException e) {
                logger.error("Cannot accept mail because of a "
                        + "maildrop failure", e);
                throw new RejectExceptionExt(e.errorStatus());
            }
            OutputStream out;
            try {
                out = appender.getOutputStream();
            } catch (LocalMailSystemException e) {
                logger.error("Cannot accept mail because of a "
                        + "maildrop failure", e);
                appender.rollback();
                throw new RejectExceptionExt(e.errorStatus());
            }
            try {
                out.write(constructReturnPathLine(mail));
                mail.mailData.writeTo(out);
            } catch (IOException e) {
                logger.error(
                        "Cannot accept mail because of an IO error "
                                + "occured while the mail was written into the maildrop",
                        e);
                appender.rollback();
                throw new RejectExceptionExt(
                        EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
            }
            try {
                appender.commit();
            } catch (LocalMailSystemException e) {
                logger.error("Cannot accept mail because of a "
                        + "maildrop failure", e);
                throw new RejectExceptionExt(e.errorStatus());
            }
        } finally {
            maildropRepository.releaseMaildrop(maildrop);
        }
    }

    private byte[] constructReturnPathLine(Mail mail) {
        try {
            return ("Return-Path: <" + mail.from + ">\r\n")
                    .getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @x.category GETSET
     */
    public void setMaildropName(String maildropName) {
        this.maildropName = maildropName;
    }

    /**
     * @x.category GETSET
     */
    public String getMaildropName() {
        return maildropName;
    }

    /**
     * @x.category GETSET
     */
    public void setMaildropRepository(MaildropRepository maildropRepository) {
        this.maildropRepository = maildropRepository;
    }

    /**
     * @x.category GETSET
     */
    public MaildropRepository getMaildropRepository() {
        return maildropRepository;
    }

    @Override
    public String toString() {
        return "MaildropDestination [maildropName=" + maildropName + "]";
    }

}
