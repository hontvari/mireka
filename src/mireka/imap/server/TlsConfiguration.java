package mireka.imap.server;

import java.io.IOException;
import javax.net.ssl.SSLServerSocket;

/**
 * TlsConfiguration provides a factory method which creates configured SSLServerSocket instances.
 */
public interface TlsConfiguration {

    /**
     * Returns an unbound server socket.
     */
    SSLServerSocket createServerSocket() throws IOException;

}