package mireka.smtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

/**
 * ClientFactory creates {@link SmartClient} instances based on the configured
 * parameters.
 */
public class ClientFactory {
    private String helo;
    private String bind;

    public SmartClient create(InetAddress inetAddress)
            throws UnknownHostException, IOException, SMTPException {
        return create(inetAddress, 25);
    }

    public SmartClient create(InetAddress inetAddress, int port)
            throws UnknownHostException, SMTPException, IOException {
        SocketAddress bindpoint = bind == null ? null : new InetSocketAddress(
                bind, 0);

        return new SmartClient(inetAddress.getHostAddress(), port, bindpoint,
                helo);
    }

    /**
     * @category GETSET
     */
    public String getHelo() {
        return helo;
    }

    /**
     * @category GETSET
     */
    public void setHelo(String helo) {
        this.helo = helo;
    }

    /**
     * @category GETSET
     */
    public String getBind() {
        return bind;
    }

    /**
     * @category GETSET
     */
    public void setBind(String bind) {
        this.bind = bind;
    }

}
