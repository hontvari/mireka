package mireka.pop.command;

import java.io.IOException;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.Session;

public class NoopCommand implements Command {
    private final Session session;

    public NoopCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException {
        session.getThread().sendResponse("+OK");
    }

}
