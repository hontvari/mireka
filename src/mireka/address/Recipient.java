package mireka.address;

/**
 * Use {@link MailAddressFactory} to create a new instance.
 * <p>
 * All implementing classes must implement the
 * {@link RemotePartContainingRecipient} interface except the special global
 * postmaster recipient.
 * 
 * @see GlobalPostmaster
 */
public interface Recipient {

    boolean isPostmaster();

    boolean isGlobalPostmaster();

    boolean isDomainPostmaster();

    String sourceRouteStripped();
}