package mireka.pop;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.destination.MailDestination;
import mireka.login.User;
import mireka.pop.store.Maildrop;
import mireka.pop.store.MaildropAppender;
import mireka.pop.store.MaildropRepository;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;

/**
 * MaildropDestination puts the mail into the specified POP3 maildrop.
 */
public class MaildropDestination implements MailDestination {
    private final Logger logger = LoggerFactory
            .getLogger(MaildropDestination.class);
    private MaildropRepository maildropRepository;
    private User user;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        Maildrop maildrop = maildropRepository.borrowMaildrop(user);
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
                mail.maildata.writeTo(out);
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
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setMaildropRepository(MaildropRepository maildropRepository) {
        this.maildropRepository = maildropRepository;
    }

    @Override
    public String toString() {
        return "MaildropDestination [user=" + user + "]";
    }

}
