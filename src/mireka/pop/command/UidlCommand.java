package mireka.pop.command;

import java.io.IOException;
import java.util.List;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;
import mireka.pop.SessionState;
import mireka.pop.store.MaildropPopException;
import mireka.pop.store.UidListing;

public class UidlCommand implements Command {
    private final Session session;

    public UidlCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != SessionState.TRANSACTION)
            throw new IllegalSessionStateException();

        List<String> args = commandParser.parseArguments();
        if (args.isEmpty())
            displayList();
        else
            displaySingleListing(commandParser.parseSingleNumericArgument());
    }

    private void displayList() throws IOException {
        List<UidListing> listings = session.getMaildrop().getUidListings();
        session.getThread().sendResponse("+OK");
        ResultListWriter writer =
                new ResultListWriter(session.getThread().getOutputStream());
        for (UidListing listing : listings) {
            writer.writeLine(listing.toString());
        }
        writer.endList();

    }

    private void displaySingleListing(int messageNumber) throws IOException,
            MaildropPopException {
        UidListing listing;
        try {
            listing = session.getMaildrop().getUidListing(messageNumber);
        } catch (IllegalArgumentException e) {
            session.getThread().sendResponse("-ERR " + e.getMessage());
            return;
        }
        session.getThread().sendResponse("+OK " + listing.toString());
    }

}
