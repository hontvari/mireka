package mireka.pop.command;

import java.io.IOException;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;
import mireka.pop.SessionState;

public class StatCommand implements Command {

    private final Session session;

    public StatCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != SessionState.TRANSACTION)
            throw new IllegalSessionStateException();

        StringBuilder response = new StringBuilder();
        response.append("+OK ");
        response.append(session.getMaildrop().getCountOfMessages());
        response.append(' ');
        response.append(session.getMaildrop().getTotalOctets());
        session.getThread().sendResponse(response.toString());
    }

}
