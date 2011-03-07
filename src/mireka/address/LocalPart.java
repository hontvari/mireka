package mireka.address;

/**
 * LocalPart contains the local part of a forward path (usually a mailbox) which
 * appeared as the recipient in an RCPT command submitted by an SMTP client,
 * e.g. "john" if the recipient was john@example.com. An SMTP server must
 * preserve the case of its characters, it must not assume that the
 * corresponding mailbox name is case insensitive. However, a mailbox name
 * should indeed be case insensitive.
 */
public class LocalPart {
    private String receivedRawText;

    public LocalPart(String escapedText) {
        this.receivedRawText = escapedText;
    }

    /**
     * Returns the unescaped mailbox name, which means that escaping constructs
     * are replaced by their represented value. For example if the raw string
     * received is "Joe\,Smith", then this function returns "Joe,Smith", without
     * the quotes. However, unescaping is not implemented currently, this
     * function simply returns the raw text.
     */
    public String displayableName() {
        return receivedRawText;
    }

    /**
     * Returns the escaped / quoted local-part string as received from the
     * remote SMTP client.
     */
    public String escapedName() {
        return receivedRawText;
    }

    /**
     * The same as {@link #displayableName()}.
     */
    @Override
    public String toString() {
        return displayableName();
    }
}
