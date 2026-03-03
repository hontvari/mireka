package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.SessionState.SELECTED;
import static mireka.imap.acl.Right.EXPUNGE;

import java.io.IOException;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.acl.NoPermissionException;
import mireka.imap.parser.CommandParser;
import mireka.imap.update.UnilateralResponseOption;

public class ExpungeCommand extends Command {

    public ExpungeCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        if (!hasAccess(EXPUNGE))
            throw new NoPermissionException();
        UnilateralResponseOption option = new UnilateralResponseOption();
        session.mailbox.expunge(option);
        return OK;
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(SELECTED);
    }

}
