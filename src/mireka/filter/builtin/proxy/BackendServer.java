package mireka.filter.builtin.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.enterprise.context.ApplicationScoped;

import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

@ApplicationScoped
public class BackendServer {
    private BackendClient client;
    private String host;
    private int port = 25;

    public SmartClient connect() throws UnknownHostException, SMTPException,
            IOException {
        SocketAddress bindpoint =
                client.getBind() == null ? null : new InetSocketAddress(client
                        .getBind(), 0);
        return new SmartClient(host, port, bindpoint, client.getHelo());
    }

    /**
     * @category GETSET
     */
    public BackendClient getClient() {
        return client;
    }

    /**
     * @category GETSET
     */
    public void setClient(BackendClient client) {
        this.client = client;
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
