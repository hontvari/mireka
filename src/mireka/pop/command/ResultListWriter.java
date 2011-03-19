package mireka.pop.command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResultListStream can be used to send POP3 list responses to LIST and UIDL
 * commands with reduced logging. On DEBUG level it logs only the count of
 * response lines instead of each line. On TRACE level it logs all lines. It
 * also buffers the output.
 */
public class ResultListWriter {
    private final Logger logger = LoggerFactory
            .getLogger(ResultListWriter.class);
    private final Writer writer;
    private int lineCount = 0;

    public ResultListWriter(OutputStream outStream) {
        try {
            this.writer =
                    new BufferedWriter(new OutputStreamWriter(outStream,
                            "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Assertion failed");
        }
    }

    /**
     * Sends and - depending on the log level - logs the supplied line.
     * 
     * @param line
     *            the line to be written out, without EOL characters.
     */
    public void writeLine(String line) throws IOException {
        logger.trace("Server: " + line);
        writer.write(line);
        writer.write("\r\n");
        lineCount++;
    }

    /**
     * Writes out the closing dot only line, flushes the buffer, and logs the
     * count of lines sent.
     */
    public void endList() throws IOException {
        writer.write(".\r\n");
        writer.flush();
        logger.debug(lineCount + " lines were sent");
    }

}
