package mireka.smtp.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * ClientFactory creates {@link SmtpClient} instances based on the configured
 * parameters.
 */
public class ClientFactory {
    private String helo;
    private String bind;

    public SmtpClient create() {
        SmtpClient client = new SmtpClient();
        client.setHeloHost(helo);
        SocketAddress bindpoint =
                bind == null ? null : new InetSocketAddress(bind, 0);
        client.setBindpoint(bindpoint);
        return client;
    }

    /**
     * @x.category GETSET
     */
    public String getHelo() {
        return helo;
    }

    /**
     * @x.category GETSET
     */
    public void setHelo(String helo) {
        this.helo = helo;
    }

    /**
     * @x.category GETSET
     */
    public String getBind() {
        return bind;
    }

    /**
     * @x.category GETSET
     */
    public void setBind(String bind) {
        this.bind = bind;
    }

}
