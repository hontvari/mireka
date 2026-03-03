package mireka.imap.command;

import static mireka.imap.SessionState.SELECTED;

import java.util.EnumSet;

import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;

/**
 * These commands can run as subcommands of the UID command.
 */
public abstract class UidSubcommand extends Command {
    protected boolean uidmode;

    public UidSubcommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(SELECTED);
    }

    public void setUidMode() {
        uidmode = true;
    }
}
