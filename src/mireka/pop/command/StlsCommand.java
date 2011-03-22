package mireka.pop.command;

import static mireka.pop.SessionState.*;

import java.io.IOException;

import javax.net.ssl.SSLSocket;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.IllegalSessionStateException;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The STLS command switches on TLS.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc2595">RFC 2595 Using TLS with
 *      IMAP, POP3 and ACAP - 4. POP3 STARTTLS extension</a>
 */
public class StlsCommand implements Command {
    private final Logger logger = LoggerFactory.getLogger(StlsCommand.class);
    private final Session session;

    public StlsCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException,
            Pop3Exception {
        if (session.getSessionState() != AUTHORIZATION)
            throw new IllegalSessionStateException();
        if (session.isTlsStarted())
            throw new Pop3Exception(null,
                    "Command not permitted when TLS active");
        session.getThread().sendResponse("+OK Begin TLS negotiation");
        SSLSocket sslSocket =
                session.getServer().getTlsConfiguration()
                        .createSSLSocket(session.getThread().getSocket());
        sslSocket.startHandshake();
        logger.debug("Cipher suite: " + sslSocket.getSession().getCipherSuite());
        session.getThread().setSocket(sslSocket);
        session.setTlsStarted(true);
    }

}
