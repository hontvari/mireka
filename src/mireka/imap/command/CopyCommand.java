package mireka.imap.command;

import static mireka.imap.ResponseCode.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mireka.imap.Completion;
import mireka.imap.CopyuidResponseCode;
import mireka.imap.ImapException;
import mireka.imap.MailId;
import mireka.imap.MessageFlagSet;
import mireka.imap.NonExistentException;
import mireka.imap.ResponseCode;
import mireka.imap.SequenceSet;
import mireka.imap.Session;
import mireka.imap.acl.Right;
import mireka.imap.parser.CommandParser;
import mireka.imap.store.Mail;
import mireka.imap.store.MailIterator;
import mireka.imap.store.Mailbox;
import mireka.imap.store.MailboxName;

public class CopyCommand extends UidSubcommand {
    private SequenceSet sequences;
    private MailboxName dstname;
    private String dst;

    public CopyCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        parser.take(' ');
        sequences = parser.parseSequenceSet();
        sequences.uid = uidmode;
        parser.take(' ');
        dst = parser.parseAstring("mailbox");
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        dstname = repository().parseMailboxName(session.user, dst);
        Mailbox dstmailbox = repository().queryMailbox(dstname);
        if (dstmailbox == null)
            throw new NonExistentException(canCreateMailbox(dstname) ? TRYCREATE : NONEXISTENT);
        accessControl().checkAccess(dstname, Right.INSERT);

        List<Long> srcUids = new ArrayList<>();
        List<Long> dstUids = new ArrayList<>();
        try (MailIterator it = mailbox().list(sequences)) {
            Mail mail;
            while ((mail = it.next()) != null) {
                MessageFlagSet flags = new MessageFlagSet();
                flags.addAll(mail.flags());
                applyFlagPermissions(dstname, flags);

                MailId id = dstmailbox.append(flags, mail.date(), mail.body());
                srcUids.add(mail.uid());
                dstUids.add(id.uid);
            }
        }

        ResponseCode code = null;
        if (accessControl().hasAccess(dstname, Right.READ)) {
            SequenceSet srcSeq = SequenceSet.fromIdList(srcUids);
            SequenceSet dstSeq = SequenceSet.fromIdList(dstUids);
            code = new CopyuidResponseCode(mailbox().status().uidvalidity, srcSeq, dstSeq);
        }
        return Completion.ok(code);

    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

}
