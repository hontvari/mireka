package mireka.filter.local.table;

import mireka.filter.Destination;
import mireka.filter.proxy.BackendServer;

/**
 * A Relay destination means that the mail must be transmitted to a gateway step
 * by step within the mail transaction in which it is received.
 */
public class Relay implements Destination {
    private BackendServer backendServer;

    public Relay(BackendServer backendServer) {
        this.backendServer = backendServer;
    }

    /**
     * @category GETSET
     */
    public BackendServer getBackendServer() {
        return backendServer;
    }

    /**
     * @category GETSET
     */
    public void setBackendServer(BackendServer backendServer) {
        this.backendServer = backendServer;
    }

}
