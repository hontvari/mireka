package mireka.pop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mireka.pop.command.ApopCommand;
import mireka.pop.command.CapaCommand;
import mireka.pop.command.DeleCommand;
import mireka.pop.command.ListCommand;
import mireka.pop.command.NoopCommand;
import mireka.pop.command.PassCommand;
import mireka.pop.command.QuitCommand;
import mireka.pop.command.RetrCommand;
import mireka.pop.command.RsetCommand;
import mireka.pop.command.StatCommand;
import mireka.pop.command.StlsCommand;
import mireka.pop.command.TopCommand;
import mireka.pop.command.UidlCommand;
import mireka.pop.command.UserCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class selects and runs the {@link Command} corresponding to the POP3
 * command line received from the client, moreover it processes the connection
 * open and close events.
 */
class CommandHandler {
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final Session session;

    /**
     * Keys are upper case command names
     */
    private final Map<String, Command> commandMap =
            new HashMap<String, Command>();
    private ApopCommand apopCommand;

    public CommandHandler(Session session) {
        this.session = session;
        commandMap.put("NOOP", new NoopCommand(session));
        commandMap.put("CAPA", new CapaCommand(session));
        commandMap.put("QUIT", new QuitCommand(session));
        UserCommand userCommand = new UserCommand(session);
        commandMap.put("USER", userCommand);
        commandMap.put("PASS", new PassCommand(session, userCommand));
        commandMap.put("STAT", new StatCommand(session));
        commandMap.put("LIST", new ListCommand(session));
        commandMap.put("UIDL", new UidlCommand(session));
        commandMap.put("RETR", new RetrCommand(session));
        commandMap.put("DELE", new DeleCommand(session));
        commandMap.put("RSET", new RsetCommand(session));
        commandMap.put("TOP", new TopCommand(session));
        apopCommand = new ApopCommand(session);
        commandMap.put("APOP", apopCommand);
        if (session.getServer().getTlsConfiguration().isEnabled())
            commandMap.put("STLS", new StlsCommand(session));
    }

    public void handleCommand(String line) throws IOException {
        try {
            CommandParser commandParser = new CommandParser(line);
            String key = commandParser.extractCommand().toUpperCase(Locale.US);
            Command command = commandMap.get(key);
            if (command == null)
                throw new Pop3Exception(null, "Command is not implemented");
            command.execute(commandParser);
        } catch (Pop3Exception e) {
            session.getThread().sendResponse(e.toResponse());
        }
    }

    /**
     * Displays the welcome message
     */
    public void sendWelcomeMessage() throws IOException {
        session.getThread().sendResponse(
                "+OK " + session.getServer().getHostName()
                        + " Mireka POP3 server ready "
                        + apopCommand.generateTimeStamp());
    }

    /**
     * Processes both expected and unexpected disconnections
     */
    public void connectionClosed() {
        if (session.getSessionState() == SessionState.TRANSACTION) {
            session.getMaildrop().rollbackTransaction();
            logger.debug("Maildrop transaction is rolled back");
        }
        if (session.getMaildrop() != null)
            session.getServer().getMaildropRepository()
                    .releaseMaildrop(session.getMaildrop());
    }

}
