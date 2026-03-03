package mireka.imap.command;

import java.io.IOException;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;

public class SearchCommand extends Command {

    public SearchCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        // TODO Auto-generated method stub

    }

    @Override
    public Completion execute() throws IOException, ImapException {
        throw new UnsupportedOperationException("Not yet");
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        // TODO Auto-generated method stub
        return null;
    }

}
