package mireka.pop.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.CommandSyntaxException;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;
import mireka.pop.SessionState;

import org.subethamail.smtp.io.CRLFTerminatedReader.MaxLineLengthException;
import org.subethamail.smtp.io.ExtraDotOutputStream;

public class TopCommand implements Command {

    private final Session session;

    public TopCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != SessionState.TRANSACTION)
            throw new IllegalSessionStateException();

        List<String> args = commandParser.parseArguments();
        if (args.size() != 2)
            throw new CommandSyntaxException(
                    "Two numeric arguments are expected");
        int messageNumber;
        int lines;
        try {
            messageNumber = Integer.valueOf(args.get(0));
            lines = Integer.valueOf(args.get(1));
        } catch (NumberFormatException e) {
            throw new CommandSyntaxException(
                    "Two numeric arguments are expected");
        }
        if (lines < 0)
            throw new CommandSyntaxException(
                    "Two numeric arguments are expected");

        InputStream mailAsStream =
                session.getMaildrop().getMailAsStream(messageNumber);
        try {
            session.getThread().sendResponse("+OK");
            ExtraDotOutputStream dotOutputStream =
                    new ExtraDotOutputStream(session.getThread()
                            .getOutputStream());
            CrLfInputStream in = new CrLfInputStream(mailAsStream);
            byte[] buffer = new byte[1000];
            int cRead;
            // read headers
            while (true) {
                cRead = in.readLineWithEol(buffer);
                if (cRead == -1)
                    break; // no body
                dotOutputStream.write(buffer, 0, cRead);
                if (buffer[0] == '\r' || buffer[0] == '\n')
                    break;
            }
            // read body
            int cLines = 0;
            while (cLines < lines) {
                try {
                    cRead = in.readLineWithEol(buffer);
                } catch (MaxLineLengthException e) {
                    // not a big problem, some body lines will be missing
                    break;
                }
                if (cRead == -1)
                    break; // end of mail
                dotOutputStream.write(buffer, 0, cRead);
                cLines++;
            }

            dotOutputStream.flush();
        } finally {
            mailAsStream.close();
        }
        session.getThread().sendResponse(".");
    }
}
