package mireka.smtp.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import mireka.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandlerFactory;

public class SMTPServer extends org.subethamail.smtp.server.SMTPServer {
    private final Logger logger = LoggerFactory.getLogger(SMTPServer.class);

    public SMTPServer(MessageHandlerFactory handlerFactory) {
        super(handlerFactory);
        setSoftwareName("Mireka " + Version.getVersion());
    }

    @Override
    @PostConstruct
    public void start() {
        super.start();
    }

    @Override
    @PreDestroy
    public void stop() {
        super.stop();
        logger.info("SMTP server stopped");
    }

    public void setBindAddress(String bindAddress) {
        try {
            setBindAddress(InetAddress.getByName(bindAddress));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
