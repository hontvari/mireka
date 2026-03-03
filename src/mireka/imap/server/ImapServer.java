package mireka.imap.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.SettingsRepo;
import mireka.imap.acl.AccessControl;
import mireka.imap.store.Repository;
import mireka.login.LoginSpecification;

/**
 * ImapServer contains configuration information and lifecycle management for an IMAP service.
 */
public class ImapServer {
    /** Host name used if we can't find one */
    private final static String UNKNOWN_HOSTNAME = "localhost";

    private final Logger logger = LoggerFactory.getLogger(ImapServer.class);

    /**
     * The address to which the listening socket will bind. It is stored as a
     * String instead of InetAddress, so it could be configured using CanDI.
     * Null means all interfaces.
     */
    private String bindAddress = null;

    private int port = 993;

    private int maximumConnections = 100;

    private TlsConfiguration tlsConfiguration = new PrivateTlsConfiguration();

    /**
     * The host name that will be reported to POP clients
     */
    private String hostName;

    private ServerThread serverThread;

    private LoginSpecification loginSpecification;

    //private PrincipalMaildropTable principalMaildropTable;

    private Repository repository;
    public AccessControl accessControl;
    public SettingsRepo settingsRepo;

    public ImapServer() {
        try {
            this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            this.hostName = UNKNOWN_HOSTNAME;
        }
    }

    @PostConstruct
    public void start() {
        logger.info("Starting IMAP server {}...", getName());
        if (serverThread != null)
            throw new IllegalStateException("IMAP server already started");

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

        ServerSocket serverSocket = tlsConfiguration.createServerSocket();

        // http://java.sun.com/j2se/1.5.0/docs/api/java/net/ServerSocket.html#setReuseAddress(boolean)
        serverSocket.setReuseAddress(true);
        serverSocket.bind(isa, 0);

        return serverSocket;
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Stopping IMAP service {}", getName());
        serverThread.shutdownNow();
        logger.info("IMAP service {} stopped", getName());
    }

    /**
     * @return the name used in log messages to refer to this server instance
     */
    public String getName() {
        return getDisplayableLocalSocketAddress();
    }

    public String getDisplayableLocalSocketAddress() {
        return (bindAddress == null ? "*" : bindAddress) + ":" + port;
    }

    /**
     * @x.category GETSET
     */
    public String getBindAddress() {
        return bindAddress;
    }

    /**
     * @x.category GETSET
     */
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * @x.category GETSET
     */
    public int getPort() {
        return port;
    }

    /**
     * @x.category GETSET
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @x.category GETSET
     */
    public int getMaximumConnections() {
        return maximumConnections;
    }

    /**
     * @x.category GETSET
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
     * @x.category GETSET
     */
    @Inject
    public void setLoginSpecification(LoginSpecification loginSpecification) {
        this.loginSpecification = loginSpecification;
    }

    /**
     * @x.category GETSET
     */
    public LoginSpecification getLoginSpecification() {
        return loginSpecification;
    }

    /**
     * @x.category GETSET
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * @x.category GETSET
     */
//    public PrincipalMaildropTable getPrincipalMaildropTable() {
//        return principalMaildropTable;
//    }

    /**
     * @x.category GETSET
     */
//    public void setPrincipalMaildropTable(
//            PrincipalMaildropTable principalMaildropTable) {
//        this.principalMaildropTable = principalMaildropTable;
//    }

    /**
     * @x.category GETSET
     */
    public void setTlsConfiguration(TlsConfiguration tlsExtension) {
        this.tlsConfiguration = tlsExtension;
    }

    /**
     * @x.category GETSET
     */
    public TlsConfiguration getTlsConfiguration() {
        return tlsConfiguration;
    }

    @Inject
    public void setAccessControl(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    @Inject
    public void setSettingsRepo(SettingsRepo settingsRepo) {
        this.settingsRepo = settingsRepo;
    }

}
