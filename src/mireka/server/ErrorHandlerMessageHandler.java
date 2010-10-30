package mireka.server;

import java.io.IOException;
import java.io.InputStream;

import mireka.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class ErrorHandlerMessageHandler implements
        MessageHandler {
    private final Logger logger = LoggerFactory
            .getLogger(ErrorHandlerMessageHandler.class);

    private final FilterChainMessageHandler wrapped;

    public ErrorHandlerMessageHandler(
            FilterChainMessageHandler wrappedHandler) {
        this.wrapped = wrappedHandler;
    }

    @Override
    public void from(String from) throws RejectException {
        try {
            wrapped.from(from);
        } catch (ConfigurationException e) {
            logger.error("Wrong configuration.", e);
            throw new RejectException(554, "Mail server configuration is wrong");
        } catch (RuntimeException e) {
            logger.error("Unexpected exception.", e);
            throw new RejectException(550, "Internal error");
        }
    }

    @Override
    public void recipient(String recipient) throws RejectException {
        try {
            wrapped.recipient(recipient);
        } catch (ConfigurationException e) {
            logger.error("Wrong configuration.", e);
            throw new RejectException(554, "Mail server configuration is wrong");
        } catch (RuntimeException e) {
            logger.error("Unexpected exception.", e);
            throw new RejectException(550, "Internal error");
        }
    }

    @Override
    public void data(InputStream data) throws RejectException,
            TooMuchDataException, IOException {
        try {
            wrapped.data(data);
        } catch (ConfigurationException e) {
            logger.error("Wrong configuration.", e);
            throw new RejectException(554, "Mail server configuration is wrong");
        } catch (RuntimeException e) {
            logger.error("Unexpected exception.", e);
            throw new RejectException(550, "Internal error");
        }
    }

    @Override
    public void done() {
        try {
            wrapped.done();
        } catch (ConfigurationException e) {
            logger.error("Wrong configuration.", e);
        } catch (RuntimeException e) {
            logger.error("Unexpected exception.", e);
        }
    }

}
