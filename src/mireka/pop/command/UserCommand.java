package mireka.pop.command;

import static mireka.pop.SessionState.*;

import java.io.IOException;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;

public class UserCommand implements Command {
    private final Session session;

    public String user;

    public UserCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != AUTHORIZATION)
            throw new IllegalSessionStateException();
        user = commandParser.parseSingleArgument();
        session.setSessionState(AUTHORIZATION_PASS_COMMAND_EXPECTED);
        session.getThread().sendResponse("+OK Proceed to password");
    }

}
