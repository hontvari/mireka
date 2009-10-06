package mireka.filter;

import java.io.InputStream;

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
     */
    InputStream getInputStream();
}
