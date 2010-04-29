package mireka.filter.builtin.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.enterprise.context.ApplicationScoped;

import mireka.ClientFactory;

import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

@ApplicationScoped
public class BackendServer {
    private ClientFactory clientFactory;
    private String host;
    private int port = 25;

    public SmartClient connect() throws UnknownHostException, SMTPException,
            IOException {
        InetAddress inetAddress = InetAddress.getByName(host);
        return clientFactory.create(inetAddress, port);
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
