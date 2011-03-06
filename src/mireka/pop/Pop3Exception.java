package mireka.pop;

import javax.annotation.Nullable;

/**
 * Signals POP3 related exception and optionally includes standard POP3 response
 * codes in addition to the human readable message.
 * 
 * @see <a
 *      href="http://www.iana.org/assignments/pop3-extension-mechanism">Standard
 *      response codes assigned by IANA</a>
 */
public class Pop3Exception extends Exception {
    private static final long serialVersionUID = 4112841660836603755L;

    /**
     * An extended POP3 response code.
     */
    @Nullable
    public final String responseCode;

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
    public Pop3Exception(String responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    /**
     * Returns a complete POP3 response line corresponding to this error, which
     * can be sent to the client.
     * 
     * @return the POP3 response line, which includes the starting -ERR
     *         characters, the optional extended response code and the human
     *         readable message.
     */
    public String toResponse() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("-ERR");
        if (responseCode != null)
            buffer.append(" [").append(responseCode).append(']');
        if (getMessage() != null)
            buffer.append(' ').append(getMessage());
        return buffer.toString();
    }

}
