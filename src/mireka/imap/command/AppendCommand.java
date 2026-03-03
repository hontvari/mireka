package mireka.imap.command;

import static mireka.imap.Completion.ok;
import static mireka.imap.ResponseCode.*;
import static mireka.imap.SessionState.*;

import java.io.IOException;
import java.time.Instant;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.MailId;
import mireka.imap.MessageFlagSet;
import mireka.imap.NonExistentException;
import mireka.imap.ResponseCode;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.acl.Right;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.Literal;
import mireka.imap.store.Mailbox;
import mireka.imap.store.MailboxName;

public class AppendCommand extends Command {

    private MailboxName mbname;
    private MessageFlagSet flags = new MessageFlagSet();
    private MailId mailId;

    public AppendCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        Instant date = null;
        take(' ');
        String name = parser.parseAstring("mailbox");
        take(' ');
        if (parser.next() == '(') {
            flags = parser.parseFlagList();
            take(' ');
        }
        if (parser.isDquote()) {
            date = parser.parseDateTime().toInstant();
            take(' ');
        }
        Literal literal = parser.parseLiteralHeader();

        mbname = repository().parseMailboxName(session.user, name);
        Mailbox mailbox = repository().queryMailbox(mbname);
        if (mailbox == null)
            throw new NonExistentException(canCreateMailbox(mbname) ? TRYCREATE : NONEXISTENT);
        accessControl().checkAccess(mbname, Right.INSERT);
        applyFlagPermissions(mbname, flags);
        mailId = mailbox.append(flags, date, literal.getLimitedInputStream());
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        ResponseCode code = null;
        if (accessControl().hasAccess(mbname, Right.READ))
            code = ResponseCode.of("APPENDUID " + mailId.uidvalidity + " " + mailId.uid);
        return ok(code);
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(AUTHENTICATED, SELECTED);
    }

}
