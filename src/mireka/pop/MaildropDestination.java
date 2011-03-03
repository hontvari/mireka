package mireka.pop;

import mireka.filter.Destination;

/**
 * MaildropDestination indicates that the mail must be placed into the POP3
 * maildrop of the recipient in question.
 */
public class MaildropDestination implements Destination {
    private String maildropName;

    /**
     * @category GETSET
     */
    public void setMaildropName(String maildropName) {
        this.maildropName = maildropName;
    }

    /**
     * @category GETSET
     */
    public String getMaildropName() {
        return maildropName;
    }

}
