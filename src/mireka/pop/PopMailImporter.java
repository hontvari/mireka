package mireka.pop;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import mireka.login.GlobalUser;
import mireka.login.GlobalUsers;
import mireka.pop.store.Maildrop;
import mireka.pop.store.MaildropAppender;
import mireka.pop.store.MaildropRepository;
import mireka.transmission.LocalMailSystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import mails from remote POP3 servers to the local POP3 maildrops at system
 * startup. This is useful during migration.
 */
public class PopMailImporter {
    private final Logger logger = LoggerFactory
            .getLogger(PopMailImporter.class);
    private GlobalUsers users;
    private MaildropRepository maildropRepository;
    private String remoteHost = "localhost";
    private int remotePort = 110;
    private int totalMailCount = 0;
    private int totalUsersWithAtLeastOneMail = 0;

    @PostConstruct
    public void doImport() {
        logger.info("Importing mail from remote POP3 maildrops");
        for (GlobalUser user : users) {
            try {
                importMails(user);
            } catch (MessagingException e) {
                logger.error("Importing mails for " + user.getUsername()
                        + " failed", e);
            }
        }
        logger.info("Importing mail from remote POP3 maildrops completed, "
                + totalMailCount + " mails were imported for "
                + totalUsersWithAtLeastOneMail
                + " users who had at least one mail.");
    }

    private void importMails(GlobalUser user) throws MessagingException {
        logger.debug("Importing mail for " + user.getUsername());
        Properties properties = new Properties();
        Session session = Session.getInstance(properties);
        Store store =
                session.getStore(new URLName("pop3://" + user.getUsername()
                        + ":" + user.getPassword() + "@" + remoteHost + ":"
                        + +remotePort + "/INBOX"));
        store.connect();
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();
        int cSuccessfulMails = 0;
        // user name currently equals with the maildrop name, but this is
        // not necessarily true in general.
        String maildropName = user.getUsername().toString();
        for (Message message : messages) {
            try {
                importMail(maildropName, message);
                message.setFlag(Flags.Flag.DELETED, true);
                cSuccessfulMails++;
            } catch (Exception e) {
                logger.error("Importing a mail for " + user.getUsername()
                        + " failed", e);
            }
        }
        folder.close(true);
        store.close();
        totalMailCount += cSuccessfulMails;
        if (cSuccessfulMails > 0)
            totalUsersWithAtLeastOneMail++;
        logger.debug(cSuccessfulMails + " mails were imported for "
                + user.getUsername());
    }

    private void importMail(String maildropName, Message message)
            throws LocalMailSystemException, IOException, MessagingException {
        Maildrop maildrop = maildropRepository.borrowMaildrop(maildropName);
        try {
            MaildropAppender appender = maildrop.allocateAppender();
            try {
                OutputStream outputStream = appender.getOutputStream();
                message.writeTo(outputStream);
                appender.commit();
            } catch (IOException e) {
                appender.rollback();
                throw e;
            } catch (MessagingException e) {
                appender.rollback();
                throw e;
            }
        } finally {
            maildropRepository.releaseMaildrop(maildrop);
        }
    }

    /**
     * @category GETSET
     */
    public GlobalUsers getUsers() {
        return users;
    }

    /**
     * @category GETSET
     */
    public void setUsers(GlobalUsers users) {
        this.users = users;
    }

    /**
     * @category GETSET
     */
    public MaildropRepository getMaildropRepository() {
        return maildropRepository;
    }

    /**
     * @category GETSET
     */
    public void setMaildropRepository(MaildropRepository maildropRepository) {
        this.maildropRepository = maildropRepository;
    }

    /**
     * @category GETSET
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * @category GETSET
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    /**
     * @category GETSET
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * @category GETSET
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
