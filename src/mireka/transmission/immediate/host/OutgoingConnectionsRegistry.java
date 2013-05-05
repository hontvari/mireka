package mireka.transmission.immediate.host;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.concurrent.GuardedBy;

import mireka.smtp.EnhancedStatus;
import mireka.transmission.immediate.PostponeException;

/**
 * OutgoingConnectionsRegistry can tell if too many connections are open to a
 * specific host at the moment.
 */
public class OutgoingConnectionsRegistry {
    /**
     * 0 means no limit
     */
    private int maxConnectionsToHost = 3;
    private final Random random = new Random();

    @GuardedBy("this")
    private final Map<InetAddress, Integer> connections =
            new HashMap<InetAddress, Integer>();

    public synchronized void openConnection(InetAddress address)
            throws PostponeException {
        if (noLimit())
            return;

        Integer connectionCountObject = connections.get(address);
        int connectionCount =
                connectionCountObject == null ? 0 : connectionCountObject;
        if (connectionCount >= maxConnectionsToHost) {
            int recommendedDelay = 5 + random.nextInt(11);
            throw new PostponeException(recommendedDelay, new EnhancedStatus(
                    451, "4.4.5",
                    "Too much connections to the destination system"),
                    "There are already " + connectionCount
                            + " connections to host " + address
                            + ", it must not be connected now. "
                            + recommendedDelay
                            + " s of delay is recommended before the next "
                            + "attempt.");
        }

        connectionCount++;
        connections.put(address, connectionCount);
    }

    public synchronized void releaseConnection(InetAddress address) {
        if (noLimit())
            return;

        Integer connectionCountObject = connections.get(address);
        int connectionCount =
                connectionCountObject == null ? 0 : connectionCountObject;
        if (connectionCount <= 0)
            throw new RuntimeException("Assertion failed, connections="
                    + connections.toString() + " address=" + address);

        if (connectionCount == 1) {
            connections.remove(address);
        } else {
            connectionCount--;
            connections.put(address, connectionCount);
        }
    }

    @GuardedBy("this")
    private boolean noLimit() {
        return maxConnectionsToHost == 0;
    }

    /**
     * @category GETSET
     */
    public int getMaxConnectionsToHost() {
        return maxConnectionsToHost;
    }

    /**
     * Sets the maximum count of simultaneous connections to a single host.
     * 
     * @param maxConnectionsToHost
     *            0 means no limit
     */
    public void setMaxConnectionsToHost(int maxConnectionsToHost) {
        this.maxConnectionsToHost = maxConnectionsToHost;
    }
}
