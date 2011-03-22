package mireka.pop;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import mireka.login.LoginSpecification;
import mireka.pop.store.MaildropRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PopServer contains configuration information and lifecycle management for the
 * POP3 service.
 */
public class PopServer {
    /** Host name used if we can't find one */
    private final static String UNKNOWN_HOSTNAME = "localhost";

    private final Logger logger = LoggerFactory.getLogger(PopServer.class);

    /**
     * The address to which the listening socket will bind. It is stored as a
     * String instead of InetAddress, so it could be configured using CanDI.
     * Null means all interfaces.
     */
    private String bindAddress = null;

    private int port = 110;

    private int maximumConnections = 100;

    private TlsConfiguration tlsConfiguration = new PrivateTlsConfiguration();

    /**
     * The host name that will be reported to POP clients
     */
    private String hostName;

    private ServerThread serverThread;

    private LoginSpecification loginSpecification;

    private PrincipalMaildropTable principalMaildropTable;

    private MaildropRepository maildropRepository;

    public PopServer() {
        try {
            this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            this.hostName = UNKNOWN_HOSTNAME;
        }
    }

    @PostConstruct
    public void start() {
        logger.info("Starting POP server {}...", getName());
        if (serverThread != null)
            throw new IllegalStateException("POP server already started");

        ServerSocket serverSocket;
        try {
            serverSocket = createServerSocket();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        serverThread = new ServerThread(serverSocket, this);
        serverThread.start();
    }

    private ServerSocket createServerSocket() throws IOException {
        InetSocketAddress isa;

        if (this.bindAddress == null) {
            isa = new InetSocketAddress(this.port);
        } else {
            isa = new InetSocketAddress(this.bindAddress, this.port);
        }

        ServerSocket serverSocket = new ServerSocket();
        // http://java.sun.com/j2se/1.5.0/docs/api/java/net/ServerSocket.html#setReuseAddress(boolean)
        serverSocket.setReuseAddress(true);
        serverSocket.bind(isa, 0);

        return serverSocket;
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Stopping POP3 service {}", getName());
        serverThread.shutdown();
        logger.info("POP3 service {} stopped", getName());
    }

    /**
     * @return the name used in log messages to refer to this server instance
     */
    private String getName() {
        return getDisplayableLocalSocketAddress();
    }

    public String getDisplayableLocalSocketAddress() {
        return (bindAddress == null ? "*" : bindAddress) + ":" + port;
    }

    /**
     * @category GETSET
     */
    public String getBindAddress() {
        return bindAddress;
    }

    /**
     * @category GETSET
     */
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * @category GETSET
     */
    public int getPort() {
        return port;
    }

    /**
     * @category GETSET
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @category GETSET
     */
    public int getMaximumConnections() {
        return maximumConnections;
    }

    /**
     * @category GETSET
     */
    public void setMaximumConnections(int maximumConnections) {
        this.maximumConnections = maximumConnections;
    }

    /** @return the host name that will be reported to SMTP clients */
    public String getHostName() {
        if (this.hostName == null)
            return UNKNOWN_HOSTNAME;
        else
            return this.hostName;
    }

    /** The host name that will be reported to SMTP clients */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @category GETSET
     */
    public void setLoginSpecification(LoginSpecification loginSpecification) {
        this.loginSpecification = loginSpecification;
    }

    /**
     * @category GETSET
     */
    public LoginSpecification getLoginSpecification() {
        return loginSpecification;
    }

    /**
     * @category GETSET
     */
    public MaildropRepository getMaildropRepository() {
        return maildropRepository;
    }

    /**
     * @category GETSET
     */
    public void setMaildropRepository(MaildropRepository maildropRepository) {
        this.maildropRepository = maildropRepository;
    }

    /**
     * @category GETSET
     */
    public PrincipalMaildropTable getPrincipalMaildropTable() {
        return principalMaildropTable;
    }

    /**
     * @category GETSET
     */
    public void setPrincipalMaildropTable(
            PrincipalMaildropTable principalMaildropTable) {
        this.principalMaildropTable = principalMaildropTable;
    }

    /**
     * @category GETSET
     */
    public void setTlsConfiguration(TlsConfiguration tlsExtension) {
        this.tlsConfiguration = tlsExtension;
    }

    /**
     * @category GETSET
     */
    public TlsConfiguration getTlsConfiguration() {
        return tlsConfiguration;
    }

}
