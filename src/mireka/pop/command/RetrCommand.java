package mireka.pop.command;

import java.io.IOException;
import java.io.InputStream;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;
import mireka.pop.SessionState;
import mireka.pop.store.ScanListing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.DotTerminatedOutputStream;
import org.subethamail.smtp.io.ExtraDotOutputStream;

public class RetrCommand implements Command {
    private final Logger logger = LoggerFactory.getLogger(RetrCommand.class);

    private final Session session;

    public RetrCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != SessionState.TRANSACTION)
            throw new IllegalSessionStateException();

        int messageNumber = commandParser.parseSingleNumericArgument();
        ScanListing scanListing =
                session.getMaildrop().getScanListing(messageNumber);
        InputStream mailAsStream =
                session.getMaildrop().getMailAsStream(messageNumber);
        try {
            session.getThread().sendResponse(
                    "+OK " + scanListing.length + " octets");
            DotTerminatedOutputStream dotTerminatedOutputStream =
                    new DotTerminatedOutputStream(session.getThread()
                            .getOutputStream());
            ExtraDotOutputStream dotOutputStream =
                    new ExtraDotOutputStream(dotTerminatedOutputStream);
            byte[] buffer = new byte[4096];
            int cRead;
            while (-1 != (cRead = mailAsStream.read(buffer))) {
                dotOutputStream.write(buffer, 0, cRead);
            }
            dotOutputStream.flush();
            dotTerminatedOutputStream.writeTerminatingSequence();
            dotTerminatedOutputStream.flush();
            logger.debug("Message sent");
        } finally {
            mailAsStream.close();
        }
    }
}
