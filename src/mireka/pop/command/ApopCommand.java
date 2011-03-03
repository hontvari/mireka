package mireka.pop.command;

import static mireka.pop.SessionState.*;

import java.io.IOException;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import mireka.login.LoginDecision;
import mireka.login.LoginResult;
import mireka.login.LoginSpecification;
import mireka.pop.CommandParser;
import mireka.pop.CommandSyntaxException;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApopCommand extends AbstractLoginCommand {
    private final Logger logger = LoggerFactory.getLogger(ApopCommand.class);
    private final LoginSpecification loginSpecification;
    private String timestamp;
    @GuardedBy("ApopCommand.class")
    private static long uniqueClockValue;

    public ApopCommand(Session session) {
        super(session);
        loginSpecification = session.getServer().getLoginSpecification();
    }

    public String generateTimeStamp() {
        if (timestamp != null)
            throw new IllegalStateException();

        long clockValue = generateUniqueClockValue();
        int random = (int) Math.round(Math.random() * 100000);
        timestamp =
                "<" + random + "." + clockValue + "@"
                        + session.getServer().getHostName() + ">";
        return timestamp;
    }

    private static long generateUniqueClockValue() {
        long millis = System.currentTimeMillis();
        synchronized (ApopCommand.class) {
            if (millis > uniqueClockValue) {
                uniqueClockValue = millis;
            } else {
                ++uniqueClockValue;
            }
            return uniqueClockValue;
        }
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != AUTHORIZATION)
            throw new IllegalSessionStateException();

        List<String> args = commandParser.parseArguments();
        if (args.size() != 2)
            throw new CommandSyntaxException("Two arguments are expected");
        String user = args.get(0);
        String digest = args.get(1);
        byte[] digestBytes = valueOfHex(digest);
        LoginResult result =
                loginSpecification.evaluateApop(user, timestamp, digestBytes);
        if (result.decision == LoginDecision.VALID) {
            startTransaction(result.principal);
        } else {
            logger.debug("Unsuccessful login result: {}", result.decision);
            session.getThread().sendResponse("-ERR [AUTH] permission denied");
            session.setSessionState(AUTHORIZATION);
        }
    }

    private static byte[] valueOfHex(String s) throws CommandSyntaxException {
        if (s.length() != 32)
            throw new CommandSyntaxException(
                    "Second argument must be a 16 bytes hexadecimal number");
        byte[] result = new byte[16];
        for (int i = 0; i < 16; i++) {
            String byteString = s.substring(i * 2, i * 2 + 2);
            try {
                result[i] = (byte) Integer.parseInt(byteString, 16);
            } catch (NumberFormatException e) {
                throw new CommandSyntaxException(
                        "Second argument must be a 16 bytes hexadecimal number");
            }
        }
        return result;
    }
}
