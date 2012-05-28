package mireka.filter.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import mireka.smtp.ClientFactory;

import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

public class BackendServer {
    private ClientFactory clientFactory;
    private String host;
    private int port = 25;

    /**
     * 
     * @throws UnknownHostException
     *             if the IP address of the backend server could not be
     *             determined based on its domain name.
     */
    public SmartClient connect() throws UnknownHostException, SMTPException,
            IOException {
        InetAddress inetAddress = InetAddress.getByName(host);
        return clientFactory.create(inetAddress, port);
    }

    @Override
    public String toString() {
        return "BackendServer [" + host + ":" + port + "]";
    }

    /**
     * @category GETSET
     */
    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    /**
     * @category GETSET
     */
    public void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * @category GETSET
     */
    public String getHost() {
        return host;
    }

    /**
     * @category GETSET
     */
    public void setHost(String host) {
        this.host = host;
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

}
