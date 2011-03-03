package mireka.filter.proxy;

import mireka.filter.Destination;

/**
 * A RelayDestination means that the mail must be transmitted to a gateway step
 * by step within the mail transaction in which it is received.
 */
public class RelayDestination implements Destination {
    private BackendServer backendServer;

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
