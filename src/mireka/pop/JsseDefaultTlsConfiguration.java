package mireka.pop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * JsseDefaultTlsConfiguration delegates TLS configuration to the system
 * property based JSSE reference implementation configuration. It enables the
 * TLS extension if the javax.net.ssl.keyStore system property is specified, and
 * uses the default JSSE socket factory to create new sockets.
 * <p>
 * The minimal necessary JSSE configuration:
 * <ul>
 * <li>javax.net.ssl.keyStore system property must refer to a file containing a
 * JKS keystore with the private key.
 * <li>javax.net.ssl.keyStorePassword system property must specify the keystore
 * password.
 * </ul>
 * 
 * @see <a
 *      href="http://download.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#InstallationAndCustomization">Customizing
 *      JSSE</a>
 */
public class JsseDefaultTlsConfiguration implements TlsConfiguration {

    @Override
    public boolean isEnabled() {
        return System.getProperty("javax.net.ssl.keyStore") != null;
    }

    @Override
    public SSLSocket createSSLSocket(Socket socket) throws IOException {
        SSLSocketFactory socketFactory =
                ((SSLSocketFactory) SSLSocketFactory.getDefault());
        InetSocketAddress remoteAddress =
                (InetSocketAddress) socket.getRemoteSocketAddress();
        SSLSocket sslSocket =
                (SSLSocket) (socketFactory.createSocket(socket,
                        remoteAddress.getHostName(), socket.getPort(), true));

        // we are a server
        sslSocket.setUseClientMode(false);
        return sslSocket;
    }

}
