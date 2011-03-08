package mireka.list;

import javax.mail.internet.ParseException;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;

/**
 * Member holds information about a mailing or forword list member.
 */
public class Member {
    private Recipient recipient;
    private String name;
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

}
