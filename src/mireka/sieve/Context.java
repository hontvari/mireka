package mireka.sieve;

import java.io.InputStream;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CompletionException;
import mireka.imap.MessageFlagSet;
import mireka.imap.store.Mailbox;
import mireka.imap.store.MailboxName;
import mireka.imap.store.Repository;
import mireka.login.User;
import mireka.sieve.CallbackException.Reason;
import mireka.transmission.Mail;

public class Context {
    public final Logger logger = LoggerFactory.getLogger(Context.class);
    public Repository repository;
    public User user;
    public MailboxName mbname;
    public Instant date;
    public InputStream prefixed;
    public Mail mail;
    /**
     * true if the stop action command is executed, it means the script execution must be stopped,
     * implicit keep must be executed as necessary.
     */
    public boolean stop;
    /**
     * True if at the end of the script an implicit keep action must be performed.
     */
    public boolean implicitKeep = true;
    /**
     * internal flag set variable for the {@link Capability#Imap4flags} extension
     */
    public MessageFlagSet messageFlags = new MessageFlagSet();

    public void store(String name, MessageFlagSet flags) throws CallbackException {
        try {
            MailboxName mbname = repository.parseMailboxName(user, name);
            Mailbox mailbox = repository.queryMailbox(mbname);
            if (mailbox == null) {
                logger.error("Mailbox does not exist, even though it should: {}", mbname);
                throw new CallbackException(Reason.MAILBOX_NOT_EXISTS);
            }
            mailbox.append(flags, date, prefixed);
        } catch (CompletionException e) {
            throw new CallbackException(Reason.UNAVAILABLE, e);
        }
    }

}
