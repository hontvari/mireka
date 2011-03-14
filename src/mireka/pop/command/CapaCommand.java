package mireka.pop.command;

import java.io.IOException;

import mireka.Version;
import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.Session;

public class CapaCommand implements Command {
    private final Session session;

    public CapaCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException {
        session.getThread().sendResponse("+OK Capability list follows");
        session.getThread().sendResponse("TOP");
        session.getThread().sendResponse("USER");
        session.getThread().sendResponse("RESP-CODES");
        session.getThread().sendResponse("AUTH-RESP-CODE");
        session.getThread().sendResponse("PIPELINING");
        session.getThread().sendResponse("UIDL");
        session.getThread().sendResponse(
                "IMPLEMENTATION Mireka-" + Version.getVersion());
        session.getThread().sendResponse(".");
    }
}
