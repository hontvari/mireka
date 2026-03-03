package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.SessionState.AUTHENTICATED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;

public class EnableCommand extends Command {

    private List<String> caps = new ArrayList<>();

    public EnableCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        while (is(' ')) {
            take();
            caps.add(parser.parseAtom("capability"));
        }
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        List<String> enabledCaps = new ArrayList<>();
        for (String cap : caps) {
            switch (cap) {
            case "IMAP4rev2":
                if (session.enableCapability(cap))
                    enabledCaps.add(cap);
            case "UTF8=ACCEPT":
                if (session.enableCapability(cap))
                    enabledCaps.add(cap);
            default:
                // ignore unknown capabilities or capabilities which are not permitted to be enabled
            }
        }
        out.respondEnabled(enabledCaps);
        return OK;
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(AUTHENTICATED);
    }

}
