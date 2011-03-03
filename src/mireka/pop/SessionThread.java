package mireka.pop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.CRLFTerminatedReader;

/**
 * SessionThread manages the TCP connection to the POP3 client and contains the
 * loop which processes the incoming commands.
 */
public class SessionThread extends Thread {
    private static final int TEN_MINUTES = 10 * 60 * 1000;
    private final Logger log = LoggerFactory.getLogger(SessionThread.class);
    private final ServerThread serverThread;
    private final PopServer server;
    private final CommandHandler commandHandler;
    /** I/O to the client */
    private Socket socket;
    private InputStream input;
    /**
     * Remark: POP3 command limit is 255 octets according to RFC 2449 #4
     */
    private CRLFTerminatedReader reader;
    private Writer writer;
    /** Set this true when doing an ordered shutdown */
    private volatile boolean quitting = false;
    private Session session;

    public SessionThread(PopServer server, ServerThread serverThread,
            Socket socket) throws IOException {
        super(SessionThread.class.getName() + "-" + socket.getInetAddress()
                + ":" + socket.getPort());
        this.server = server;
        this.serverThread = serverThread;
        setSocket(socket);
        session = new Session(server, this);
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
        if (log.isDebugEnabled()) {
            InetAddress remoteInetAddress =
                    this.getRemoteAddress().getAddress();
            remoteInetAddress.getHostName(); // Causes future toString() to
                                             // print the name too
            log.debug("POP3 connection from {}, new connection count: {}",
                    remoteInetAddress, serverThread.getNumberOfConnections());
        }

        try {
            if (serverThread.hasTooManyConnections()) {
                log.debug("POP3 Too many connections!");

                this.sendResponse("-ERR [SYS/TEMP] Too many connections, try again later");
                return;
            }

            commandHandler.sendWelcomeMessage();

            while (!this.quitting) {
                try {
                    String line = null;
                    try {
                        line = this.reader.readLine();
                    } catch (SocketException ex) {
                        // Lots of clients just "hang up" rather than issuing
                        // QUIT, which would
                        // fill our logs with the warning in the outer catch.
                        if (log.isDebugEnabled())
                            log.debug(
                                    "Error reading client command: "
                                            + ex.getMessage(), ex);

                        return;
                    }

                    if (line == null) {
                        log.debug("no more lines from client");
                        return;
                    }

                    logClientLineSecurely(line);
                    commandHandler.handleCommand(line);
                } catch (SocketTimeoutException ex) {
                    // according to RFC 1939 no response should be sent on
                    // timeout
                    log.debug("Socket timeout: " + ex.getMessage());
                    return;
                } catch (CRLFTerminatedReader.TerminationException te) {
                    String msg =
                            "-ERR Syntax error at character position "
                                    + te.position()
                                    + ". CR and LF must be CRLF paired.  See RFC 1939 #3";

                    log.debug(msg);
                    this.sendResponse(msg);

                    // if people are screwing with things, close connection
                    return;
                } catch (CRLFTerminatedReader.MaxLineLengthException mlle) {
                    String msg = "-ERR " + mlle.getMessage();

                    log.debug(msg);
                    this.sendResponse(msg);

                    // if people are screwing with things, close connection
                    return;
                }
            }
        } catch (IOException e1) {
            if (!this.quitting) {
                try {
                    // Send a temporary failure back so that the server will try
                    // to resend
                    // the message later.
                    this.sendResponse("-ERR [SYS/TEMP] Problem attempting to execute commands. Please try again later.");
                } catch (IOException e) {
                    // it is expected that a response for an IO error cannot be
                    // sent
                }

                if (log.isWarnEnabled())
                    log.warn("Exception during POP session", e1);
            }
        } finally {
            this.closeConnection();
            this.notifyCommandHandlerOnDisconnect();
        }

    }

    /** Sends the response to the client */
    public void sendResponse(String response) throws IOException {
        if (log.isDebugEnabled())
            log.debug("Server: " + response);

        this.writer.write(response + "\r\n");
        this.writer.flush();
    }

    /**
     * It logs the line but masks out any clear text passwords
     */
    private void logClientLineSecurely(String line) {
        if (!log.isDebugEnabled())
            return;
        if (line.toUpperCase(Locale.US).startsWith("PASS ")) {
            line = line.substring(0, 5) + "*****";
        }
        log.debug("Client: " + line);
    }

    /**
     * Close reader, writer, and socket, logging exceptions but otherwise
     * ignoring them
     */
    private void closeConnection() {
        try {
            try {
                this.writer.close();
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
        if ((this.socket != null) && this.socket.isBound()
                && !this.socket.isClosed())
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

    public OutputStream getOutputStream() throws IOException {
        writer.flush();
        return socket.getOutputStream();
    }

    public void shutdown() {
        quit();
    }

    /**
     * Triggers the shutdown of the thread and the closing of the connection.
     */
    public void quit() {
        quitting = true;
        closeConnection();
    }

    /**
     * Initializes our reader, writer, and the i/o filter chains based on the
     * specified socket. This is called internally when we startup and when (if)
     * SSL is started.
     */
    public void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.input = this.socket.getInputStream();
        this.reader = new CRLFTerminatedReader(this.input);
        this.writer =
                new OutputStreamWriter(this.socket.getOutputStream(),
                        "US-ASCII");

        this.socket.setSoTimeout(TEN_MINUTES);
    }

    private InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) this.socket.getRemoteSocketAddress();
    }

}
