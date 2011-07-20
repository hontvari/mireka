package mireka.address;

/**
 * ReversePath represents the address to which bounce messages should be sent,
 * it is supplied in the SMTP MAIL command.
 */
public interface ReversePath {
    boolean isNull();

    /**
     * Returns the raw reverse path text, as it was supplied in the SMTP
     * transaction, without the angle brackets.
     */
    public String getSmtpText();

    /**
     * Returns the displayable form of the reverse path.
     */
    public String toString();

}
