package mireka.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;

@Startup
@ApplicationScoped
public class SMTPService {
    private final Logger logger = LoggerFactory.getLogger(SMTPService.class);
    public static final int DEFAULT_PORT = 25;
    private SMTPServer smtpServer;
    /**
     * Corresponds to the similar attribute in SMTPServer, but stored as a
     * String instead of InetAddress, so it could be configured using CanDI.
     */
    private String bindAddress;

    // @Inject
    public SMTPService(SMTPServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    @PostConstruct
    public void start() {
        smtpServer.setBindAddress(getBindInetAddress());
        logger.info("Starting SMTP service {}", getName());
        smtpServer.start();
        logger.info("SMTP service {} started", getName());
    }

    private String getName() {
        return (smtpServer.getBindAddress() == null ? "*" : smtpServer
                .getBindAddress())
                + ":" + smtpServer.getPort();
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopping SMTP service {}", getName());
        smtpServer.stop();
        logger.info("SMTP service {} stopped", getName());
    }

    /**
     * converts {@link #bindAddress} to InetAddress without checked exception,
     * CDI will deal with it anyway.
     */
    private InetAddress getBindInetAddress() {
        try {
            return InetAddress.getByName(bindAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }
}
