package mireka.imap.server;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.parser.LineHead;

public class ProtocolLogger {
    private final static Logger logger = LoggerFactory.getLogger(ProtocolLogger.class);
    private final static Logger streamLogger = LoggerFactory
            .getLogger(ProtocolLogger.class.getPackageName() + ".ProtocolLoggerStream");
    /**
     * logs literals byte by byte with a byte position within the literal.
     */
    private final static Logger literalLogger = LoggerFactory
            .getLogger(ProtocolLogger.class.getPackageName() + ".ProtocolLoggerLiteral");
    private final String connection;
    private final AtomicLong serverStreamCounter = new AtomicLong();
    private final AtomicLong clientStreamCounter = new AtomicLong();
    public volatile String mailbox;

    public ProtocolLogger(String connection) {
        this.connection = connection;
    }

    public void logServer(LineHead line) {
        if (logger.isDebugEnabled())
            logger.debug(context() + "Server: " + line.toString());
    }

    public void logClient(LineHead line) {
        if (logger.isDebugEnabled())
            logger.debug(context() + "Client: " + line.toString());
    }

    public void logClient(byte[] data) {
        if (logger.isDebugEnabled())
            logger.debug(context() + "Client: " + displayable(data, 0, data.length));
    }

    public void logServerLiteral(LineHead line) {
        if (logger.isTraceEnabled())
            logger.trace(context() + "SerLit: " + line.toString());
    }

    public void logServerStream(int b) {
        if (streamLogger.isTraceEnabled()) {
            streamLogger.trace(context() + "SerStr: {} {}", serverStreamCounter.getAndIncrement(),
                    displayable(b));
        }
    }

    public void logServerStream(byte[] b) {
        if (streamLogger.isTraceEnabled()) {
            logServerStream(b, 0, b.length);
            // serverStreamLogger.trace("SerStr: {} {}", serverStreamCounter.getAndAdd(b.length),
            // displayable(b, 0, b.length));
        }
    }

    public void logServerStream(byte[] b, int offset, int length) {
        if (streamLogger.isTraceEnabled()) {
            for (int i = offset; i < offset + length; i++) {
                logServerStream(Byte.toUnsignedInt(b[i]));
            }
            // serverStreamLogger.trace("SerStr: {} {}", serverStreamCounter.getAndAdd(length),
            // displayable(b, offset, length));
        }
    }

    public void logClientStream(int b) {
        if (streamLogger.isTraceEnabled()) {
            streamLogger.trace(context() + "CliStr: {} {}", clientStreamCounter.getAndIncrement(),
                    displayable(b));
        }
    }

    public void logClientStream(byte[] b, int offset, int length) {
        if (streamLogger.isTraceEnabled()) {
            for (int i = offset; i < offset + length; i++) {
                logClientStream(Byte.toUnsignedInt(b[i]));
            }
        }
    }

    public void logServerLiteralByte(long pos, int b) {
        if (literalLogger.isTraceEnabled())
            literalLogger.trace(context() + "LITERAL " + pos + " " + displayable(b));
    }

    private static String displayable(int b) {
        if (0x20 <= b && b <= 0x7E)
            return Character.toString(b);
        else
            return "%" + Integer.toHexString(b);
    }

    private String context() {
        String mailbox = this.mailbox;
        return connection + (mailbox == null ? "" : " " + mailbox) + " ";
    }

    private static String displayable(byte[] b, int off, int len) {
        StringBuilder buf = new StringBuilder();
        buf.append('[');
        for (int i = off; i < off + len; i++) {
            if (i != 0)
                buf.append(", ");
            int v = Byte.toUnsignedInt(b[off + i]);
            buf.append(displayable(v));
        }
        buf.append(']');
        return buf.toString();
    }
}
