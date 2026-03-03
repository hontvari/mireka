package mireka.imap.command;

import static mireka.imap.Completion.*;
import static mireka.imap.SessionState.NOT_AUTHENTICATED;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.CommandSyntaxException;
import mireka.imap.parser.LineScanner;
import mireka.imap.parser.ProtocolException;
import mireka.login.LoginDecision;
import mireka.login.LoginResult;
import mireka.login.LoginSpecification;

public class AuthenticateCommand extends Command {
    private final Logger logger = LoggerFactory.getLogger(AuthenticateCommand.class);
    private String mechanism;
    private byte[] initialResponse;

    public AuthenticateCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        take(' ');
        mechanism = parser.parseAtom("mechanism");
        initialResponse = null;
        if (parser.isSpace()) {
            parser.take();
            initialResponse = parser.parseBase64Opt("initial-response");
        }
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        if (mechanism.contentEquals("PLAIN")) {
            return authPlain(initialResponse);
        } else {
            return no("Unsupported authentication mechanism: " + mechanism);
        }
    }

    private Completion authPlain(byte[] initialResponse) throws IOException, ImapException {
        byte[] response = ensureResponse(initialResponse);
        int z1 = -1;
        int z2 = -1;
        for (int i = 0; i <= response.length; i++) {
            if (response[i] == 0) {
                z1 = i;
                break;
            }
        }
        if (z1 == -1)
            throw new CommandSyntaxException("0 separator missing");
        for (int i = z1 + 1; i <= response.length; i++) {
            if (response[i] == 0) {
                z2 = i;
                break;
            }
        }
        if (z2 == -1)
            throw new CommandSyntaxException("0 separator missing");
        // byte[] authzidBytes = Arrays.copyOfRange(response, 0, z1);
        byte[] authcidBytes = Arrays.copyOfRange(response, z1 + 1, z2);
        byte[] passwordBytes = Arrays.copyOfRange(response, z2 + 1, response.length);
        // String authzid = new String(authzidBytes, StandardCharsets.UTF_8);
        String user = new String(authcidBytes, StandardCharsets.UTF_8);
        String password = new String(passwordBytes, StandardCharsets.UTF_8);

        LoginSpecification loginSpecification = session.server.getLoginSpecification();
        LoginResult result = loginSpecification.evaluatePlain(user, password);

        if (result.decision == LoginDecision.VALID) {
            repository().createDefaultMailboxes(result.principal);
            session.setAuthenticatedState(result.principal);
            return ok(session.capabilitiesResponseCode(), "Authentication successful");
        } else {
            logger.debug("Unsuccessful authentication result: {}", result.decision);
            return no("Invalid user and password combination");
        }

    }

    private byte[] ensureResponse(byte[] initialResponse) throws IOException, ImapException {
        if (initialResponse != null)
            return initialResponse;
        out.respondCommandContinuationRequest(null);
        LineScanner scanner = new LineScanner(session.connection.input, session.protocolLogger);
        try {
            if (scanner.next == '*') {
                scanner.takeIt();
                scanner.takeEof();
                throw new ProtocolException("Cancelled");
            } else if (scanner.next == '=') {
                scanner.takeIt();
                scanner.takeEof();
                return new byte[0];
            } else {
                return Base64.getDecoder().decode(scanner.readLine());
            }
        } finally {
            scanner.skip();
        }
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(NOT_AUTHENTICATED);
    }
}
