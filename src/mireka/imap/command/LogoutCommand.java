package mireka.imap.command;

import static mireka.imap.Completion.OK_LOGOUT;
import static mireka.imap.SessionState.*;

import java.io.IOException;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;

public class LogoutCommand extends Command {

    public LogoutCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        out.respondBye("Server terminating connection");
        session.setLogoutState();
        return OK_LOGOUT;
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(NOT_AUTHENTICATED, AUTHENTICATED, SELECTED);
    }

}
