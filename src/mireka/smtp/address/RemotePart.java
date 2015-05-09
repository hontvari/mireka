package mireka.smtp.address;

/**
 * RemotePart identifies the destination system in a mail address. For example
 * in the john@example.com address example.com is the remote part.
 */
public interface RemotePart {
    /**
     * Returns the raw remote part text, as it was supplied in the SMTP
     * transaction.
     */
    public String smtpText();

    public boolean equals(Object obj);

    public int hashCode();

}
