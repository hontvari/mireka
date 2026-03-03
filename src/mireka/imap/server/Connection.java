package mireka.imap.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import mireka.imap.ResponseCode;
import mireka.imap.Session;
import mireka.imap.parser.Generator;
import mireka.imap.parser.LineHead;
import mireka.imap.parser.NoMoreLinesException;
import mireka.imap.server.ConcurrentOutputStream.Substream;
import mireka.imap.store.MailboxName;

/**
 * Connection manages the TCP connection from the IMAP client and contains the loop which processes
 * incoming commands.
 */
public class Connection extends Thread {
    private static final int PRE_AUTHENTICATION_TIMEOUT = 1 * 60 * 1000;
    /**
     * At least 30 minutes is recommended by the RFC 9051 after authentication
     */
    public static final int POST_AUTHENTICATION_TIMEOUT = 30 * 60 * 1000;
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private final Logger log = LoggerFactory.getLogger(Connection.class);
    public final ServerThread serverThread;
    private final CommandHandler commandHandler;
    /** I/O to the client */
    public final Socket socket;
    public final ProtocolLogger protocolLogger;
    public final PushbackInputStream input;
    public final ConcurrentOutputStream outputStream;
    /**
     * used within this class for greeting and error responses
     */
    private final Substream outputSubstream;
    /**
     * used within this class by this thread for greeting and error responses
     */
    private final Generator out;
    public final Session session;
    /**
     * The latch will open when the server greeting is sent
     */
    private final CountDownLatch greetingLatch = new CountDownLatch(1);
    /**
     * True if an orderly shutdown of this thread is started. Running commands will be completed but
     * no new commands will be accepted. If all running commands completes, the socket will be
     * closed.
     */
    private volatile boolean isShutdown = false;
    public Connection(ImapServer server, ServerThread serverThread, Socket socket)
            throws IOException {
        super(Connection.class.getName() + "-" + socket.getInetAddress() + ":" + socket.getPort());
        this.serverThread = serverThread;
        this.socket = socket;
        this.socket.setSoTimeout(PRE_AUTHENTICATION_TIMEOUT);
        this.protocolLogger = new ProtocolLogger(socket.getInetAddress() + ":" + socket.getPort());
        this.input = new PushbackInputStream(
                new LoggingInputStream(socket.getInputStream(), protocolLogger), 1024);
        this.outputStream = new ConcurrentOutputStream(socket.getOutputStream(), protocolLogger);
        this.outputSubstream = outputStream.createSubstream();
        this.out = new Generator(outputSubstream, protocolLogger);
        this.session = new Session(server, this);
        this.commandHandler = new CommandHandler(session);
    }

    @Override
    public void run() {
        try {
            doRun();
        } finally {
            serverThread.sessionEnded(this);
        }
    }

    private void doRun() {
        try {
            if (log.isDebugEnabled()) {
                InetAddress remoteInetAddress = ((InetSocketAddress) socket
                        .getRemoteSocketAddress()).getAddress();
                // Call getHostName, so future toString() prints the name too
                remoteInetAddress.getHostName();
                log.debug("IMAP connection from {}, connection count: {}", remoteInetAddress,
                        serverThread.getNumberOfConnections());
            }

            if (serverThread.hasTooManyConnections()) {
                log.debug("Too many connections!");
                out.respondGreetingBye(session.server.getHostName(),
                        "Too many connections, try again later");
                session.setLogoutState();
                greetingLatch.countDown();
                return;
            } else if (serverThread.shuttingDown) {
                out.respondGreetingBye(session.server.getHostName(),
                        "Server is shutting down");
                session.setLogoutState();
                greetingLatch.countDown();
                return;
            } else {
                out.respondGreetingOk(session.capabilitiesResponseCode(),
                        session.server.getHostName());
                greetingLatch.countDown();
            }

            while (!isShutdown) {
                try {
                    MailboxName mbname = session.mbname;
                    MDC.put("mailbox", mbname == null ? "" : mbname.storeName.toString());
                    commandHandler.handleCommand();
                    // logClientLineSecurely(line);
                } catch (SocketException ex) {
                    // Lots of clients just "hang up" rather than issuing
                    // QUIT
                    if (log.isDebugEnabled())
                        log.debug("Error reading client command: " + ex.getMessage(), ex);
                    return;
                } catch (NoMoreLinesException e) {
                    log.debug("no more lines from client");
                    return;
                } catch (SocketTimeoutException ex) {
                    log.debug("Socket timeout: " + ex.getMessage());
                    out.respondBye("Autologout; idle for too long");
                    return;
                }
            }
        } catch (IOException e1) {
            if (!isShutdown) {
                try {
                    // Send a temporary failure back so that the server will try to resend the
                    // message later.
                    out.respondBye(
                            "Problem attempting to execute commands. Please try again later.");
                } catch (IOException e) {
                    // it is expected that a response for an IO error cannot be sent
                }

                if (log.isWarnEnabled())
                    log.warn("Exception during POP session", e1);
            }
        } catch (Throwable e) {
            log.error("Exiting thread, closing connection after error", e);
            try {
                out.respondBye(ResponseCode.of("SERVERBUG"),
                        "Unexpected error in server, disconnecting");
            } catch (Throwable e1) {
                // ignore, the state is unknown here, the original reason is already logged
            }
        } finally {
            this.closeConnection();
            this.notifyCommandHandlerOnDisconnect();
        }

    }

    public void sendResponse(String response) throws IOException {
        LineHead line = new LineHead();
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        line.append(bytes);
        protocolLogger.logServer(line);

        try (OutputStream out = outputStream.createSubstream()) {
            out.write(bytes);
            out.write(CRLF);
        }
    }

    public void handleIoException(IOException e) {
        // TODO Auto-generated method stub

    }

    /**
     * Close reader, writer, and socket, logging exceptions but otherwise ignoring them
     */
    private void closeConnection() {
        try {
            try {
                if (outputSubstream != null)
                    outputSubstream.close();
                this.outputStream.close();
                this.input.close();
            } finally {
                this.closeSocket();
            }
        } catch (IOException e) {
            log.info(e.toString());
        }
    }

    /** Close the client socket if it is open */
    private void closeSocket() throws IOException {
        if ((this.socket != null) && this.socket.isBound() && !this.socket.isClosed())
            this.socket.close();
    }

    /** Safely calls connectionClosed() on the command handler */
    private void notifyCommandHandlerOnDisconnect() {
        try {
            commandHandler.connectionClosed();
        } catch (Exception ex) {
            log.error("Exception in command handler", ex);
        }
    }

    /**
     * Starts an orderly shutdown. This function will return when all commaands are finished and all
     * pending notifications are sent.
     * 
     * @param humanReadableText null means that the BYE response is already sent
     */
    public void shutdownNow(String humanReadableText) {
        try {
            greetingLatch.await();
            if (humanReadableText != null)
                new Generator(outputStream.createSubstream(), protocolLogger)
                        .respondBye(humanReadableText);
            session.commands.shutdown();
            session.commands.awaitTermination();
            session.setLogoutState();
            session.commands.sendUpdatesBeforeShutdown();
        } catch (InterruptedException e) {
            log.warn("connection shutdown interrupted", e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.warn("IOException during connection shutdown", e);
        } finally {
            isShutdown = true;
            closeConnection();
        }
    }

}
