package mireka.pop.command;

import java.io.IOException;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.Session;
import mireka.pop.SessionState;
import mireka.pop.store.MaildropPopException;

public class QuitCommand implements Command {
    private final Session session;

    public QuitCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            MaildropPopException {
        if (session.getSessionState() == SessionState.TRANSACTION) {
            session.setSessionState(SessionState.UPDATE);
            session.getMaildrop().commitTransaction();
        }
        session.getThread().sendResponse(
                "+OK " + session.getServer().getHostName()
                        + " POP3 server signing off");
        session.getThread().quit();
    }
}
