package mireka.imap;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZonedDateTime;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.Multilang;
import mireka.destination.MailDestination;
import mireka.imap.store.Mailbox;
import mireka.imap.store.MailboxName;
import mireka.imap.store.Repository;
import mireka.imap.store.Transaction;
import mireka.login.User;
import mireka.maildata.Maildata;
import mireka.maildata.type.MediaType;
import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter;
import mireka.sieve.SyntaxException;
import mireka.sieve.ast.Program;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.address.MailAddressFactory;
import mireka.transmission.Mail;

/**
 * ImapDestination puts the mail into the INBOX of the specified user.
 */
public class ImapDestination implements MailDestination {
    private static final MessageFlagSet emptyMessageFlagSet = new MessageFlagSet();

    private final Logger logger = LoggerFactory.getLogger(ImapDestination.class);
    @Inject
    public Repository repository;
    @Inject
    public SettingsRepo settingsRepo;
    public User user;
    /**
     * The destination mailbox. The default is the INBOX mailbox of the user. The mailbox name is
     * from the viewpoint of the specified user.
     */
    public String mailbox = "INBOX";
    /**
     * It overrides the sieve script location which is specified in the user settings.
     */
    @Nullable
    public String script = null;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        Maildata maildata = null;
        try {
            Transaction.init();
            repository.begin();
            repository.createDefaultMailboxes(user);
            MailboxName mbname = repository.parseMailboxName(user, this.mailbox);
            Instant date = mail.arrivalDate;

            maildata = mail.maildata.copy();
            maildata.simple.prependReturnPath(mail.from);
            maildata.save();
            InputStream in = maildata.getInputStream();
            Maildata copy = maildata.copy();
            maildata.close();
            maildata = copy;

            ByteArrayInputStream returnPathStream = new ByteArrayInputStream(
                    constructReturnPathLine(mail));
            InputStream prefixed = new SequenceInputStream(returnPathStream,
                    mail.maildata.getInputStream());
            if (script == null) {
                Mailbox mailbox = repository.queryMailbox(mbname);
                if (mailbox == null) {
                    logger.error("Mailbox does not exist, even though it should: {}", mbname);
                    throw new RejectExceptionExt(EnhancedStatus.INCORRECT_CONFIGURATION);
                }
                mailbox.append(emptyMessageFlagSet, date, prefixed);
                Transaction.commit();
            } else {
                try {
                    Context callback = new Context();
                    callback.repository = repository;
                    callback.user = user;
                    callback.mbname = mbname;
                    callback.date = date;
                    callback.prefixed = prefixed;
                    callback.mail = mail;
                    Interpreter interpreter = new Interpreter(
                            new PushbackInputStream(new FileInputStream(script)));
                    Program program = interpreter.compile();
                    program.run(callback);
                    Transaction.commit();
                } catch (SyntaxException e) {
                    logger.error("Syntax exception in sieve script: ", e);
                    sendSieveLog(e);
                    throw new RejectExceptionExt(EnhancedStatus.INCORRECT_CONFIGURATION);
                } catch (CallbackException e) {
                    switch (e.reason) {
                    case MAILBOX_NOT_EXISTS:
                        throw new RejectExceptionExt(EnhancedStatus.INCORRECT_CONFIGURATION);
                    case UNAVAILABLE:
                        throw new RejectExceptionExt(
                                EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
                    default:
                        throw new RejectExceptionExt(
                                EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
                    }
                }
            }
        } catch (ImapException e) {
            logger.error("Cannot accept mail because of an IMAP repository failure", e);
            throw new RejectExceptionExt(EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        } catch (IOException e) {
            logger.error("Cannot accept mail because of an IO error occured while the mail "
                    + "was written into the mailbox", e);
            throw new RejectExceptionExt(EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        } finally {
            if (maildata != null)
                maildata.close();
            Transaction.cleanup();
        }

    }

    private void sendSieveLog(Exception e) {
        Settings u = settingsRepo.get(user);
        Multilang ml = Multilang.get(u.locale());
        ZonedDateTime now = ZonedDateTime.now();
        Mail mail = new Mail();
        mail.arrivalDate = now.toInstant();
        mail.from = MailAddressFactory.createReversePathAlreadyVerified("");
        mail.recipients.add(
                MailAddressFactory.createRecipientAlreadyVerified(u.internalAddress().generate()));
        Maildata md = mail.maildata = new Maildata();
        md.simple.setOriginationDate(now);
        md.simple.setFrom(u.postmasterMailbox());
        md.simple.setTo(u.internalMailbox());
        md.simple.setMessageId(u.internalDomain());
        md.simple.setSubject(ml.sieveScriptSubject());
        md.simple.setMimeVersion();
        // md.simple.setContentType("text/plain;format=fixed");
        MediaType mediaType = new MediaType("text", "plain");
        // MediaParameter parameter = new MediaParameter(script, mailbox)
        // mediaType.parameters

        // md.simple.setT
        // TODO Auto-generated method stub
        
    }

    private void runScript(Mail mail) throws IOException, SyntaxException, CallbackException {
        try (FileInputStream source = new FileInputStream(script)) {
            Context callback = new Context();
            callback.repository = repository;
            callback.user = user;
            // callback.mbname = mbname;
            // callback.date = date;
            // callback.prefixed = prefixed;
            callback.mail = mail;
            Interpreter interpreter = new Interpreter(new PushbackInputStream(source));
            Program program = interpreter.compile();
            program.run(callback);
        }
    }

    private byte[] constructReturnPathLine(Mail mail) {
        try {
            return ("Return-Path: <" + mail.from + ">\r\n").getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ImapDestination [repository=" + repository + ", user=" + user + ", mailbox="
                + mailbox + ", script=" + script + "]";
    }

}
