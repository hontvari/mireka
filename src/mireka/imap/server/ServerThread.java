package mireka.imap.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The IMAP server thread accepts connections on the server port.
 */
public class ServerThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(ServerThread.class);
    private final ServerSocket serverSocket;
    private final ImapServer server;
    private final Semaphore connectionPermits;
    public final ConcurrentConnections connections = new ConcurrentConnections();
    public volatile boolean shuttingDown;

    ServerThread(ServerSocket serverSocket, ImapServer server) {
        super(ServerThread.class.getName() + " "
                + server.getDisplayableLocalSocketAddress());
        this.serverSocket = serverSocket;
        this.server = server;
        // reserve a few places for graceful disconnects with informative
        // messages
        this.connectionPermits =
                new Semaphore(server.getMaximumConnections() + 10);
    }

    @Override
    public void run() {
        MDC.put("localServerSocketAddress",
                server.getDisplayableLocalSocketAddress());
        logger.info("IMAP server {} started",
                server.getDisplayableLocalSocketAddress());
        while (!shuttingDown) {
            try {
                connectionPermits.acquire();
            } catch (InterruptedException e) {
                if (!shuttingDown)
                    logger.debug("Server socket thread was interrupted "
                            + "unexpectedly", e);
                Thread.currentThread().interrupt();
                break;
            }
            Connection sessionThread;
            try {
                Socket socket = serverSocket.accept();
                sessionThread = new Connection(server, this, socket);
            } catch (IOException e) {
                connectionPermits.release();
                // it also happens during shutdown, when the socket is closed
                if (!shuttingDown) {
                    logger.error("Error accepting connection", e);
                }
                continue;
            }
            // add thread before starting it,
            // because it will check the count of sessions
            synchronized (this) {
                connections.add(sessionThread.session);
                // sessionThreads.add(sessionThread);
            }
            sessionThread.start();
        }

        closeServerSocket();
        logger.info("IMAP server {} stopped accepting connections",
                server.getDisplayableLocalSocketAddress());
        MDC.remove("localServerSocketAddress");
    }

    public void shutdownNow() {
        shutdownNowServerSocket();
        connections.forEach(session -> session.connection.shutdownNow("Server is shutting down"));
    }

    private void shutdownNowServerSocket() {
        shuttingDown = true;
        this.interrupt();
        closeServerSocket();
        try {
            this.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean hasTooManyConnections() {
        synchronized (this) {
            return connections.size() > server.getMaximumConnections();
        }
    }

    public int getNumberOfConnections() {
        return connections.size();
    }

    public void sessionEnded(Connection sessionThread) {
        connections.remove(sessionThread.session);
        connectionPermits.release();
    }

    /**
     * Closes the serverSocket in an orderly way
     */
    private void closeServerSocket() {
        try {
            if (!serverSocket.isClosed())
                serverSocket.close();

            logger.debug("IMAP server socket shut down");
        } catch (IOException e) {
            logger.error("Failed to close server socket.", e);
        }
    }

}
