package mireka.filter.builtin.proxy;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.enterprise.inject.Initializer;

import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

public class LazyBackendConnector {

    private final BackendServer server;

    /**
     * null if not yet connected
     */
    private SmartClient connection;

    @Initializer
    public LazyBackendConnector(BackendServer server) {
        this.server = server;
    }

    public SmartClient connection() throws UnknownHostException, SMTPException,
            IOException {
        if (connection == null) {
            connection = server.connect();
        }
        return connection;
    }

    public void done() {
        if (connection != null)
            connection.quit();
    }
}
