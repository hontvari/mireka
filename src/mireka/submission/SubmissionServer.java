package mireka.submission;

import mireka.smtp.server.SMTPServer;

import org.subethamail.smtp.MessageHandlerFactory;

public class SubmissionServer extends SMTPServer {

    public SubmissionServer(MessageHandlerFactory handlerFactory) {
        super(handlerFactory);
        setPort(587);
    }
}
