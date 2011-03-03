package mireka.pop.command;

import java.io.IOException;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;
import mireka.pop.SessionState;

public class DeleCommand implements Command {
    private final Session session;

    public DeleCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != SessionState.TRANSACTION)
            throw new IllegalSessionStateException();
        session.getMaildrop()
                .delete(commandParser.parseSingleNumericArgument());
        session.getThread().sendResponse("+OK message deleted");
    }
}
