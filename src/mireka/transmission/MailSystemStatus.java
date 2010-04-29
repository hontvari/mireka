package mireka.transmission;

/**
 * This interface represents an SMTP status, which is returned by a remote MTA
 * in a response, or generated locally.
 */
public interface MailSystemStatus {

    /**
     * returns the original 3 digit reply codes, compatible with the original
     * SMTP RFC.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.2">rfc5321 -
     *      4.2. SMTP Replies</a>
     */
    int getSmtpReplyCode();

    /**
     * Returns the extracted text message, SMTP and enhanced status codes are
     * removed. The result may consists of one or more lines.
     */
    String getMessage();

    /**
     * Returns the original response without any parsing, for example old SMTP
     * and new enhanced codes are included on every line (if they were present).
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3461#section-9.2">RFC 3461
     *      Simple Mail Transfer Protocol (SMTP) Service Extension for Delivery
     *      Status Notifications (DSNs) - 9.2 "smtp" diagnostic-type.</a>
     */
    String getDiagnosticCode();
}