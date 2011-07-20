package mireka.address;

/**
 * The special "Postmaster" recipient (without domain name) is the only
 * Recipient which doesn't implement this interface
 */
public interface RemotePartContainingRecipient extends Recipient {
    Mailbox getMailbox();
}
