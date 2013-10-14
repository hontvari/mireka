package mireka.smtp.client;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.SendException;
import mireka.transmission.immediate.Upstream;

import org.subethamail.smtp.client.PlainAuthenticator;

/**
 * BackendServer specifies another SMTP server which is used as a proxy target
 * or smarthost. It may be part of an {@link Upstream}.
 */
public class BackendServer {
    private static final String IPV6_PREFIX = "[IPv6:";
    private static final Pattern dottedQuad = Pattern
            .compile("\\d{1,3}(\\.\\d{1,3}){3}");

    private ClientFactory clientFactory;

    /**
     * The host name in the format as it appears in the configuration. This
     * format is theoretically ambiguous, so it is only appropriate for
     * informational purposes.
     */
    private String host;
    /**
     * The host name formatted in the same way as the remote part of a mailbox.
     * For example:
     * <ul>
     * <li>mail.example.com
     * <li>[192.0.2.0]
     * <li>[IPv6:::1]
     * </ul>
     */
    private String smtpFormattedHost;

    /**
     * The IP address which was specified in the host field. It the host field
     * contains a domain name, then this field is null.
     */
    private InetAddress fixedAddress;
    private int port = 25;
    private String user;
    private String password;
    /**
     * {@link BackendServer#setWeight}
     */
    private double weight = 1;
    /**
     * {@link BackendServer#setBackup}
     */
    private boolean backup = false;

    /**
     * 
     * @throws SendException
     *             if the IP address of the backend server could not be
     *             determined based on its domain name.
     */
    public SmtpClient createClient() throws SendException {
        SmtpClient client = clientFactory.create();
        if (user != null) {
            PlainAuthenticator authenticator =
                    new PlainAuthenticator(client, user, password);
            client.setAuthenticator(authenticator);
        }
        InetAddress address;
        try {
            address =
                    fixedAddress != null ? fixedAddress : InetAddress
                            .getByName(host);
        } catch (UnknownHostException e) {
            // without detailed information, assume it is a temporary failure
            throw new SendException("Resolving the backend " + this.toString()
                    + " domain failed.", e, new EnhancedStatus(450, "4.4.0",
                    "Domain name resolution failed"));
        }
        client.setMtaAddress(new MtaAddress(smtpFormattedHost, address, port));
        return client;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    /**
     * @category GETSET
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the domain name or IP address of the backend server. The name may
     * contain a domain name or IPv4 or IPv6 literals in various forms. It
     * guesses the actual type of the name.
     * <p>
     * Examples for legal values:
     * <ul>
     * <li>mail.example.com
     * <li>[192.0.2.0]
     * <li>192.0.2.0
     * <li>[IPv6:::1]
     * <li>[::1]
     * <li>::1
     * </ul>
     */
    public void setHost(String host) {
        this.host = host;
        if (host == null)
            throw new NullPointerException();
        if (host.isEmpty())
            throw new IllegalArgumentException();
        if (host.charAt(0) == '[') {
            // literal
            if (host.charAt(host.length() - 1) != ']')
                throw new IllegalArgumentException();
            if (host.length() > IPV6_PREFIX.length()
                    && IPV6_PREFIX.equalsIgnoreCase(host.substring(0,
                            IPV6_PREFIX.length())))
                setHostByAddress(host.substring(IPV6_PREFIX.length(),
                        host.length()));
            setHostByAddress(host.substring(1, host.length()));
        } else {
            if (dottedQuad.matcher(host).matches())
                setHostByAddress(host);
            if (host.contains(":"))
                setHostByAddress(host);
            setHostByDomain(host);
        }
    }

    private void setHostByDomain(String domain) {
        smtpFormattedHost = domain;
        fixedAddress = null;
    }

    private void setHostByAddress(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            if (inetAddress instanceof Inet4Address) {
                smtpFormattedHost = "[" + address + "]";
            } else if (inetAddress instanceof Inet6Address) {
                smtpFormattedHost = "[IPv6:" + address + "]";
            } else {
                throw new RuntimeException();
            }
            fixedAddress = inetAddress;
        } catch (UnknownHostException e) {
            // impossible, the argument is an IP address, not a domain name.
            throw new RuntimeException("Assertion failed");
        }
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    @Inject
    public void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
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
    public String getUser() {
        return user;
    }

    /**
     * @category GETSET
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @category GETSET
     */
    public String getPassword() {
        return password;
    }

    /**
     * @category GETSET
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @category GETSET
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Relative weight of the server in an Upstream. Default is 1.
     * 
     * @category GETSET
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * @category GETSET
     */
    public boolean isBackup() {
        return backup;
    }

    /**
     * True indicates that the server should only be used in an Upstream if all
     * non-backup servers failed. Default is false.
     * 
     * @category GETSET
     */
    public void setBackup(boolean backup) {
        this.backup = backup;
    }
}
