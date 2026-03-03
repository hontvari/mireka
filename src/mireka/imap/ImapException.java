package mireka.imap;

import javax.annotation.Nullable;

/**
 * Signals POP3 related exception and optionally includes standard POP3 response
 * codes in addition to the human readable message.
 * 
 * @see <a
 *      href="http://www.iana.org/assignments/pop3-extension-mechanism">Standard
 *      response codes assigned by IANA</a>
 */
public class ImapException extends Exception {
    private static final long serialVersionUID = 4112841660836603755L;

    /**
     * Either NO (failure) or BAD (protocol error)
     */
    public final String status;
    
    /**
     * An IMAP response code, like AUTHENTICATIONFAILED
     */
    @Nullable
    public final ResponseCode responseCode;

    /**
     * Constructs a Pop3Exception with the specified response code and human
     * readable message.
     * 
     * @param responseCode
     *            An extended POP3 response code, for example "IN-USE".
     *            Frequently this is null, because there are not many
     *            standardized POP3 codes, moreover they are useful only if the
     *            client program should react differently to different errors.
     * @param message
     *            Human readable message which will be sent to the client.
     */
    public ImapException(String status, ResponseCode responseCode, String message) {
        super(message);
        this.status = status;
        this.responseCode = responseCode;
    }

    /**
     * Returns a complete IMAP response line corresponding to this error, which can be sent to the
     * client.
     * 
     * @param tag Either a tag identifier or "*" for untagged responses.
     * 
     * @return the IMAP response line, which includes the tag, the status ("NO" or "BAD") the
     * optional response code and the human readable message.
     */
    public String toResponse(String tag) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(tag).append(' ');
        buffer.append(status);
        if (responseCode != null)
            buffer.append(" [").append(responseCode).append(']');
        if (getMessage() != null)
            buffer.append(' ').append(getMessage());
        return buffer.toString();
    }

}
