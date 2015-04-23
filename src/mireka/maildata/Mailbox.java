package mireka.maildata;

/**
 * Mailbox represents the mailbox nonterminal, which consists of an optional
 * display name and an addr-spec.
 */
public class Mailbox extends Address {
    /**
     * Null if it is not specified.
     */
    public String displayName;
    public AddrSpec addrSpec;

}
