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
    private SMTPServer smtpServer;
    /**
     * Corresponds to the similar attribute in SMTPServer, but stored as a
     * String instead of InetAddress, so it could be configured using CanDI.
     * Null means all interfaces.
     */
    private String bindAddress;

    @PostConstruct
    public void start() {
        smtpServer.setBindAddress(getBindInetAddress());
        logger.info("Starting SMTP service {}", getName());
        smtpServer.start();
        logger.info("SMTP service {} started", getName());
    }

    /**
     * converts {@link #bindAddress} to InetAddress without checked exception,
     * CDI will deal with it anyway.
     * 
     * @return null if the server must bind to all interfaces
     */
    private InetAddress getBindInetAddress() {
        if (bindAddress == null)
            return null;
        try {
            return InetAddress.getByName(bindAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
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
     * @category GETSET
     */
    public SMTPServer getSmtpServer() {
        return smtpServer;
    }

    /**
     * @category GETSET
     */
    public void setSmtpServer(SMTPServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }
}
