package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.SessionState.*;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Locale;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.LineScanner;
import mireka.imap.parser.ProtocolException;

public class IdleCommand extends Command {
    public IdleCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        out.respondCommandContinuationRequest("idling");
        session.commands.registerIdling();
        try {
            LineScanner scanner = new LineScanner(session.connection.input, session.protocolLogger);
            String response = scanner.readLine();
            if (response.toUpperCase(Locale.US).equals("DONE"))
                return OK;
            else
                throw new ProtocolException("DONE is expected");
        } finally {
            session.commands.clearIdling();
        }
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(AUTHENTICATED, SELECTED);
    }

}
