package mireka.imap.server;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

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
    public SSLServerSocket createServerSocket() throws IOException {
        SSLServerSocketFactory socketFactory =
                ((SSLServerSocketFactory) SSLServerSocketFactory.getDefault());
        return (SSLServerSocket) socketFactory.createServerSocket();
    }

}
