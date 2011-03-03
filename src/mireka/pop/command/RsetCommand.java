package mireka.pop.command;

import java.io.IOException;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;
import mireka.pop.SessionState;

public class RsetCommand implements Command {
    private final Session session;

    public RsetCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != SessionState.TRANSACTION)
            throw new IllegalSessionStateException();
        try {
            session.getMaildrop().resetDeletions();
        } catch (IllegalArgumentException e) {
            session.getThread().sendResponse("-ERR " + e.getMessage());
            return;
        }
        session.getThread().sendResponse(
                "+OK maildrop has "
                        + session.getMaildrop().getCountOfMessages()
                        + " messages ("
                        + session.getMaildrop().getTotalOctets() + ")");
    }

}
