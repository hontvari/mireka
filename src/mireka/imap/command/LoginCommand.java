package mireka.imap.command;

import static mireka.imap.Completion.*;
import static mireka.imap.SessionState.NOT_AUTHENTICATED;

import java.io.IOException;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;
import mireka.login.LoginDecision;
import mireka.login.LoginResult;
import mireka.login.LoginSpecification;

public class LoginCommand extends Command {
    private final Logger logger = LoggerFactory.getLogger(LoginCommand.class);
    private String user;
    private String password;
    
    public LoginCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        take(' ');
        user = parser.parseAstring("user");
        take(' ');
        password = parser.parseAstring("user");
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        LoginSpecification loginSpecification = session.server.getLoginSpecification();
        LoginResult result = loginSpecification.evaluatePlain(user, password);
        
        if (result.decision == LoginDecision.VALID) {
            session.setAuthenticatedState(result.principal);
            repository().createDefaultMailboxes(session.user);
            return ok(session.capabilitiesResponseCode(), "Login authentication successful");
        } else {
            logger.debug("Unsuccessful login result: {}", result.decision);
            return no("Invalid user and password combination");
        }
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(NOT_AUTHENTICATED);
    }

}
