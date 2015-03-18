package mireka.pop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import mireka.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TlsConfiguration stores configuration data for the TLS extension. The name of
 * the configuration options follow the Tomcat 7 BIO SSL configuration.
 * 
 * @see <a
 *      href="http://tomcat.apache.org/tomcat-7.0-doc/config/http.html#SSL_Support">Apache
 *      Tomcat - The HTTP Connector - SSL Support</a>
 */
public class PrivateTlsConfiguration implements TlsConfiguration {
    private final Logger logger = LoggerFactory
            .getLogger(PrivateTlsConfiguration.class);
    private boolean enabled = false;
    private String keystoreFile = "conf/keystore.jks";
    private String keystorePass = "changeit";
    private SSLSocketFactory socketFactory;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        if (!enabled)
            return;

        try {
            logger.debug("Cipher suites: "
                    + Arrays.toString(SSLContext.getDefault()
                            .getDefaultSSLParameters().getCipherSuites()));
            SSLContext sslContext = SSLContext.getInstance("TLS");
            logger.debug("SSL provider: " + sslContext.getProvider());
            logger.debug("SSL protocol: " + sslContext.getProtocol());
            String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            logger.debug("Default KeyManagerFactory algorithm name: "
                    + defaultAlgorithm);
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(defaultAlgorithm);
            String defaultKeyStoreType = KeyStore.getDefaultType();
            logger.debug("Default key store type: " + defaultKeyStoreType);
            KeyStore keyStore = KeyStore.getInstance(defaultKeyStoreType);
            FileInputStream in = null;
            File actualKeystoreFile = new File(keystoreFile);
            try {
                in = new FileInputStream(actualKeystoreFile);
                keyStore.load(in, keystorePass.toCharArray());
            } catch (IOException e) {
                throw new ConfigurationException("Cannot open keyfile "
                        + actualKeystoreFile, e);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
            logger.debug("Key store size: " + keyStore.size());
            keyManagerFactory.init(keyStore, keystorePass.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            sslContext.init(keyManagers, null, null);
            socketFactory = sslContext.getSocketFactory();
        } catch (IOException e) {
            throw new ConfigurationException(e);
        } catch (GeneralSecurityException e) {
            throw new ConfigurationException(e);
        }

    }

    @Override
    public SSLSocket createSSLSocket(Socket socket) throws IOException {
        if (!enabled)
            throw new IllegalStateException();

        InetSocketAddress remoteAddress =
                (InetSocketAddress) socket.getRemoteSocketAddress();
        SSLSocket sslSocket =
                (SSLSocket) socketFactory.createSocket(socket,
                        remoteAddress.getHostName(), socket.getPort(), true);
        sslSocket.setUseClientMode(false);
        return sslSocket;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @x.category GETSET
     */
    public void setEnabled(boolean tlsEnabled) {
        this.enabled = tlsEnabled;
    }

    /**
     * @x.category GETSET
     */
    public String getKeystoreFile() {
        return keystoreFile;
    }

    /**
     * @x.category GETSET
     */
    public void setKeystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    /**
     * @x.category GETSET
     */
    public String getKeystorePass() {
        return keystorePass;
    }

    /**
     * @x.category GETSET
     */
    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }
}
