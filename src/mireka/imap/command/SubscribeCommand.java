package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.SessionState.*;

import java.io.IOException;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.NonExistentException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.acl.Right;
import mireka.imap.parser.CommandParser;
import mireka.imap.store.MailboxName;

public class SubscribeCommand extends Command {

    private String name;

    public SubscribeCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        take(' ');
        name = parser.parseAstring("mailbox");
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        MailboxName mbname = repository().parseMailboxName(session.user, name);
        if (repository().queryMailbox(mbname) == null)
            throw new NonExistentException();
        if (!accessControl().hasAccess(mbname, Right.LOOKUP))
            throw new NonExistentException();
        repository().subscribe(mbname);
        return OK;
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
