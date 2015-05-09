package mireka.forward;

import java.text.ParseException;

import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Recipient;

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

    @Override
    public String toString() {
        return recipient.toString();
    }

    /**
     * @x.category GETSET
     */
    public void setAddress(String mailbox) {
        try {
            this.recipient = new MailAddressFactory().createRecipient(mailbox);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid configuration", e);
        }
    }

    /**
     * @x.category GETSET
     */
    public Recipient getRecipient() {
        return recipient;
    }

    /**
     * @x.category GETSET
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @x.category GETSET
     */
    public String getName() {
        return name;
    }

    /**
     * @x.category GETSET
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @x.category GETSET
     */
    public boolean isDisabled() {
        return disabled;
    }

}
