package mireka.pop;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The POP3 server thread accepts connections on the server port.
 */
class ServerThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(ServerThread.class);
    private final ServerSocket serverSocket;
    private final PopServer server;
    private final Semaphore connectionPermits;
    private volatile boolean shuttingDown;
    @GuardedBy("this")
    private final Set<SessionThread> sessionThreads =
            new HashSet<SessionThread>(200);

    ServerThread(ServerSocket serverSocket, PopServer server) {
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
        logger.info("POP server {} started",
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
            SessionThread sessionThread;
            try {
                Socket socket = serverSocket.accept();
                sessionThread = new SessionThread(server, this, socket);
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
                sessionThreads.add(sessionThread);
            }
            sessionThread.start();
        }

        closeServerSocket();
        logger.info("POP server {} stopped",
                server.getDisplayableLocalSocketAddress());
        MDC.remove("localServerSocketAddress");
    }

    public void shutdown() {
        shutdownServerSocket();
        shutdownSessions();
    }

    private void shutdownServerSocket() {
        shuttingDown = true;
        closeServerSocket();
        interrupt();
    }

    private void shutdownSessions() {
        synchronized (this) {
            for (SessionThread sessionThread : sessionThreads) {
                sessionThread.shutdown();
            }
        }
    }

    public boolean hasTooManyConnections() {
        synchronized (this) {
            return sessionThreads.size() > server.getMaximumConnections();
        }
    }

    public int getNumberOfConnections() {
        synchronized (this) {
            return sessionThreads.size();
        }
    }

    public void sessionEnded(SessionThread sessionThread) {
        synchronized (this) {
            sessionThreads.remove(sessionThread);
        }
        connectionPermits.release();
    }

    /**
     * Closes the serverSocket in an orderly way
     */
    private void closeServerSocket() {
        try {
            if (!serverSocket.isClosed())
                serverSocket.close();

            logger.debug("POP server socket shut down");
        } catch (IOException e) {
            logger.error("Failed to close server socket.", e);
        }
    }

}
