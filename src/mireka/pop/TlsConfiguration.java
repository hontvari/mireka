package mireka.pop;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

/**
 * TlsConfiguration provides information about whether the TLS extension is
 * enabled and provides a factory method which creates configured SSLSocket
 * instances.
 */
public interface TlsConfiguration {

    /**
     * Returns true if TLS is enabled.
     */
    boolean isEnabled();

    /**
     * Returns an SSLSocket which wraps the supplied non-secured socket. The
     * returned socket is in server mode.
     * 
     * @param socket
     *            the socket to be wrapped
     */
    SSLSocket createSSLSocket(Socket socket) throws IOException;

}