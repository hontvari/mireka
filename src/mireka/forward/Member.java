package mireka.forward;

import javax.mail.internet.ParseException;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;

/**
 * Member holds information about a mailing or forward list member.
 */
public class Member {
    private Recipient recipient;
    private String name;
    /**
     * True if the member is temporarily suspended. The entry itself is not
     * deleted, but the member must be considered as a non-member.
     */
    private boolean disabled;

    /**
     * @category GETSET
     */
    public void setAddress(String mailbox) {
        try {
            this.recipient = new MailAddressFactory().createRecipient(mailbox);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid configuration", e);
        }
    }

    /**
     * @category GETSET
     */
    public Recipient getRecipient() {
        return recipient;
    }

    /**
     * @category GETSET
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @category GETSET
     */
    public String getName() {
        return name;
    }

    /**
     * @category GETSET
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @category GETSET
     */
    public boolean isDisabled() {
        return disabled;
    }

}
