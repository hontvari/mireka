package mireka.pop;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.Settings;
import mireka.imap.SettingsRepo;
import mireka.login.Userlist;
import mireka.login.User;
import mireka.pop.store.Maildrop;
import mireka.pop.store.MaildropAppender;
import mireka.pop.store.MaildropRepository;
import mireka.transmission.LocalMailSystemException;

/**
 * Import mails from remote POP3 servers to the local POP3 maildrops at system
 * startup. This is useful during migration.
 */
public class PopMailImporter {
    private final Logger logger = LoggerFactory
            .getLogger(PopMailImporter.class);
    @Inject
    private Userlist users;
    @Inject
    public SettingsRepo settingsRepo;
    private MaildropRepository maildropRepository;
    private String remoteHost = "localhost";
    private int remotePort = 110;
    private int totalMailCount = 0;
    private int totalUsersWithAtLeastOneMail = 0;

    @PostConstruct
    public void doImport() {
        logger.info("Importing mail from remote POP3 maildrops");
        for (User user : users) {
            try {
                importMails(user);
            } catch (MessagingException e) {
                logger.error("Importing mails for " + user
                        + " failed", e);
            }
        }
        logger.info("Importing mail from remote POP3 maildrops completed, "
                + totalMailCount + " mails were imported for "
                + totalUsersWithAtLeastOneMail
                + " users who had at least one mail.");
    }

    private void importMails(User user) throws MessagingException {
        logger.debug("Importing mail for " + user);
        Settings u = settingsRepo.get(user);
        Properties properties = new Properties();
        Session session = Session.getInstance(properties);
        Store store =
                session.getStore(new URLName("pop3://"
                        + u.name() + ":" + u.password()
                        + "@" + remoteHost + ":" + +remotePort + "/INBOX"));
        store.connect();
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();
        int cSuccessfulMails = 0;
        // user name currently equals with the maildrop name, but this is
        // not necessarily true in general.
        for (Message message : messages) {
            try {
                importMail(user, message);
                message.setFlag(Flags.Flag.DELETED, true);
                cSuccessfulMails++;
            } catch (Exception e) {
                logger.error("Importing a mail for " + user
                        + " failed", e);
            }
        }
        folder.close(true);
        store.close();
        totalMailCount += cSuccessfulMails;
        if (cSuccessfulMails > 0)
            totalUsersWithAtLeastOneMail++;
        logger.debug(cSuccessfulMails + " mails were imported for "
                + user);
    }

    private void importMail(User user, Message message)
            throws LocalMailSystemException, IOException, MessagingException {
        Maildrop maildrop = maildropRepository.borrowMaildrop(user);
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
     * @x.category GETSET
     */
    public MaildropRepository getMaildropRepository() {
        return maildropRepository;
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
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * @x.category GETSET
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    /**
     * @x.category GETSET
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * @x.category GETSET
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
