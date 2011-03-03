package mireka.pop.command;

import static mireka.pop.SessionState.*;

import java.io.IOException;

import mireka.login.LoginDecision;
import mireka.login.LoginResult;
import mireka.login.LoginSpecification;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassCommand extends AbstractLoginCommand {
    private final Logger logger = LoggerFactory.getLogger(PassCommand.class);
    private final UserCommand userCommand;
    private final LoginSpecification loginSpecification;

    public PassCommand(Session session, UserCommand userCommand) {
        super(session);
        this.userCommand = userCommand;
        loginSpecification = session.getServer().getLoginSpecification();
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != AUTHORIZATION_PASS_COMMAND_EXPECTED)
            throw new IllegalSessionStateException();
        String password = commandParser.parseSingleExtendedArgument();
        LoginResult result =
                loginSpecification.evaluatePlain(userCommand.user, password);
        if (result.decision == LoginDecision.VALID) {
            startTransaction(result.principal);
        } else {
            logger.debug("Unsuccessful login result: {}", result.decision);
            session.getThread().sendResponse(
                    "-ERR [AUTH] Invalid user and password combination");
            session.setSessionState(AUTHORIZATION);
        }
    }
}
