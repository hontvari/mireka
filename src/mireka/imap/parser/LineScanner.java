package mireka.imap.parser;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.server.ProtocolLogger;

/**
 * In addition to being a scanner for parsing it is also a line reader. Its specialty is that it
 * returns EOF on CRLF, and it only continue reading the stream on explicit instruction. If it reach
 * CRLF, it reads it, and from that point it returns -1 EOF token. It does not return the CRLF byte
 * pair.
 */
public class LineScanner {
    public static final int EOF = -1;
    private static final int CR = 0x0D, LF = 0x0A;
    private final Logger logger = LoggerFactory.getLogger(LineScanner.class);

    private final InputStream in;
    private final ProtocolLogger protocolLogger;
    /**
     * the next byte or -1 on EOL. (Not on EOF! An actual EOF before a CRLF causes an exception)
     */
    public int next = -1;
    /**
     * The index of {@link #next} in {@link #in}, within the current line.
     */
    int position = 0;
    /**
     * true means it reached and read EOL, the scanner returns EOF from now, until start()
     * explicitly called.
     */
    boolean stopped = false;
    LineHead line = new LineHead();

    public LineScanner(InputStream in, ProtocolLogger protocolLogger) throws IOException {
        this.in = in;
        this.protocolLogger = protocolLogger;
        readNext();
    }

    private void readNext() throws IOException {
        next = in.read();
        if (next == CR) {
            next = in.read();
            if (next == LF) {
                protocolLogger.logClient(line);
                stopped = true;
                next = EOF;
            } else {
                protocolLogger.logClient(line);
                throw new LineFormatException("LF expected after CR, but received " + next + " at "
                        + position + " line: " + line.toString());
            }
        } else if (next == LF) {
            protocolLogger.logClient(line);
            throw new LineFormatException(
                    "Unexpected LF at " + position + " line: " + line.toString());
        } else if (next == EOF) {
            protocolLogger.logClient(line);
            throw new LineFormatException(
                    "EOF before CRLF at " + position + " line: " + line.toString());
        }
    }

    public int takeIt() throws IOException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("scanner.takeIt %2X %s", next,
                    next == EOF ? "EOF" : String.valueOf((char) next)));
        if (stopped)
            return EOF;
        // it is never EOF here
        int takenChar = next;
        line.append(takenChar);
        readNext();
        position++;
        return takenChar;
    }
    
    public void takeEof() throws IOException, CommandSyntaxException {
        if (next != EOF)
            throw new CommandSyntaxException("EOL is expected at position " + position);
    }

    public void skip() throws IOException {
        while (next != EOF)
            takeIt();
    }

    public String readLine() throws IOException, CommandSyntaxException {
        while (next != EOF)
            takeIt();
        if (line.overflow)
            throw new CommandSyntaxException("Line is too long");
        return line.toString();
    }

}
