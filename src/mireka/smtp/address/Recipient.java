package mireka.smtp.address;

/**
 * Recipient is the address of the intended recipient of a mail, it is passed in
 * the RCPT SMTP command. It is either a forward path or one of the special
 * postmaster mailbox names.
 * <p>
 * Use {@link MailAddressFactory} to create a new instance.
 * <p>
 * All implementing classes must implement the
 * {@link RemotePartContainingRecipient} interface except the special global
 * postmaster recipient.
 * 
 * @see GlobalPostmaster
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.1.1">4.1.1.3.
 *      RECIPIENT</a>
 */
public interface Recipient {

    boolean isPostmaster();

    boolean isGlobalPostmaster();

    boolean isDomainPostmaster();

    LocalPart localPart();

    String sourceRouteStripped();

    /**
     * Returns the displayable (unescaped) form of the recipient, without source
     * route.
     */
    String toString();
}