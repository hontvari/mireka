package mireka.imap.command;

import static mireka.imap.Completion.OK;

import java.io.IOException;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;

public class CapabilityCommand extends Command {

    public CapabilityCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
    }

    @Override
    public Completion execute() throws ImapException, IOException {
        out.respondCapability(session.capabilities());
        return OK;
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }
    
    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.allOf(SessionState.class);
    }

}
