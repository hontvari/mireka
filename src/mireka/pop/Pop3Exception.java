package mireka.pop;

import javax.annotation.Nullable;

/**
 * Signals POP3 related exception and optionally includes standard POP3 response
 * codes in addition to the human readable message.
 */
public class Pop3Exception extends Exception {
    private static final long serialVersionUID = 4112841660836603755L;

    @Nullable
    public final String responseCode;

    /**
     * @param responseCode
     *            it may be null
     */
    public Pop3Exception(String responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    /**
     * @return POP3 response including the starting -ERR characters
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
