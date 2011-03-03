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
import mireka.pop.store.ScanListing;

public class ListCommand implements Command {

    private final Session session;

    public ListCommand(Session session) {
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
            displaySingleScanListing(commandParser.parseSingleNumericArgument());
    }

    private void displayList() throws IOException {
        List<ScanListing> scanListings =
                session.getMaildrop().getScanListings();
        long totalOctets = 0;
        for (ScanListing scanListing : scanListings) {
            totalOctets += scanListing.length;
        }
        session.getThread().sendResponse(
                "+OK " + scanListings.size() + " messages (" + totalOctets
                        + ")");
        for (ScanListing scanListing : scanListings) {
            session.getThread().sendResponse(scanListing.toString());
        }
        session.getThread().sendResponse(".");

    }

    private void displaySingleScanListing(int messageNumber)
            throws IOException, MaildropPopException {
        ScanListing scanListing;
        try {
            scanListing = session.getMaildrop().getScanListing(messageNumber);
        } catch (IllegalArgumentException e) {
            session.getThread().sendResponse("-ERR " + e.getMessage());
            return;
        }
        session.getThread().sendResponse("+OK " + scanListing.toString());
    }

}
