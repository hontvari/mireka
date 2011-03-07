package mireka;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the material transmitted after the SMTP DATA command is accepted
 * and before the end of data indication is transmitted.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-2.3.9">RFC 5321
 *      2.3.9. Message Content and Mail Data</a>
 */
public interface MailData {
    /**
     * Returns the data stream positioned to the first byte of the mail data.
     * The caller must close the returned stream.
     * 
     * @throws IOException
     */
    InputStream getInputStream() throws IOException;

    /**
     * Copies the message content into the supplied stream. It does not close
     * the target stream.
     */
    void writeTo(OutputStream out) throws IOException;

    /**
     * Releases resources, like temporary files.
     */
    void dispose();
}
