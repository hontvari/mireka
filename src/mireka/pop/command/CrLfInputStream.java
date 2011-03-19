package mireka.pop.command;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.io.MaxLineLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.CRLFTerminatedReader.MaxLineLengthException;

/**
 * CrLfInputStream recognizes CR LF line endings but otherwise assumes arbitrary
 * binary content. Mail content is special, because it is considered to be an
 * octet stream, although usually it contains text. MIME mail headers can be
 * encoded in either US-ASCII or UTF-8, but the mail body can be in any charset,
 * moreover it can be arbitrary binary data encoded using the MIME binary
 * method, so the result does not even similar to text. The POP3 TOP command
 * needs to return mail headers and a few lines of the body. The latter is
 * obsolete functionality, there may be no lines in the body, but we have to
 * deal with this situation.
 */
public class CrLfInputStream {
    private final Logger logger = LoggerFactory
            .getLogger(CrLfInputStream.class);
    private final InputStream in;
    private int pushBackChar;
    private boolean isPushBackFilled = false;
    private boolean isInvalidLineEndingLogged = false;

    public CrLfInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Reads a line without end-of-line characters. It recognizes but logs line
     * endings other then CR LF.
     * 
     * @throws MaxLineLengthException
     *             if the line is longer than the length of the buffer
     */
    public int readLineWithEol(byte[] buffer) throws MaxLineLengthException,
            IOException {
        int i = 0;
        while (true) {
            int octet = readInput();
            if (octet == -1) {
                return i == 0 ? -1 : i;
            } else if (octet == '\r') {
                buffer[i++] = (byte) octet;
                octet = readInput();
                if (octet == '\n') {
                    if (i >= buffer.length)
                        throw new MaxLineLimitException(
                                "Input line length is too long!");
                    buffer[i++] = (byte) octet;
                    return i;
                } else {
                    logInvalidLineEnding();
                    pushBack(octet);
                    return i;
                }
            } else if (octet == '\n') {
                logInvalidLineEnding();
                buffer[i++] = (byte) octet;
                return i;
            } else {
                buffer[i++] = (byte) octet;
            }
            if (i >= buffer.length)
                throw new MaxLineLimitException(
                        "Input line length is too long!");
        }
    }

    private int readInput() throws IOException {
        if (isPushBackFilled) {
            isPushBackFilled = false;
            return pushBackChar;
        } else {
            return in.read();
        }
    }

    private void pushBack(int ch) {
        if (isPushBackFilled)
            throw new RuntimeException("Assertion failed");
        isPushBackFilled = true;
        pushBackChar = ch;
    }

    private void logInvalidLineEnding() {
        if (isInvalidLineEndingLogged)
            return;
        logger.debug("Invalid line ending in input stream");
        isInvalidLineEndingLogged = true;
    }
}
