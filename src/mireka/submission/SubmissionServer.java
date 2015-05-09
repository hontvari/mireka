package mireka.submission;

import mireka.smtp.server.SMTPServer;

import org.subethamail.smtp.MessageHandlerFactory;

/**
 * A Submission service implements the RFC 6409 Message Submission service, it
 * accepts a mail for transmission to local or remote domains from authenticated
 * user agents.
 * 
 * This class is only a minor (indirect) extension to the SubEthaSMTP SMTPServer
 * class.
 * 
 * @see org.subethamail.smtp.server.SMTPServer
 * 
 */
public class SubmissionServer extends SMTPServer {

    public SubmissionServer(MessageHandlerFactory handlerFactory) {
        super(handlerFactory);
        setPort(587);
    }
}
