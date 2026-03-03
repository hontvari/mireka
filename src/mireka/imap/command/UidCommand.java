package mireka.imap.command;

import static mireka.imap.SessionState.SELECTED;

import java.io.IOException;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.CommandSyntaxException;

public class UidCommand extends Command {
    private UidSubcommand subcommand;

    public UidCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        parser.extractSubcommand();
        createSubcommand(parser.subcommand);
        subcommand.parse();
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        return subcommand.execute();
    }

    private void createSubcommand(String command) throws CommandSyntaxException {
        switch (command) {
        case "FETCH":
            subcommand = new FetchCommand(session, parser);
            break;
        case "STORE":
            subcommand = new StoreCommand(session, parser);
            break;
        case "COPY":
            subcommand = new CopyCommand(session, parser);
            break;
        case "EXPUNGE":
            subcommand = new UidExpungeCommand(session, parser);
            break;
        default:
            throw new CommandSyntaxException("Unknown UID subcommand");
        }
        subcommand.setUidMode();
        subcommand.parent = this;
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        return subcommand.asyncExecute();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(SELECTED);
    }

}
