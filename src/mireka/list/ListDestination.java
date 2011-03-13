package mireka.list;

import mireka.destination.MailDestination;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

/**
 * A ListDestination assigned to a recipient indicates that the mail should be
 * redistributed to multiple recipients.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-3.9">RFC 5321 3.9
 *      Mailing Lists and Aliases</a>
 */
public class ListDestination implements MailDestination {
    private MailingList list;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        list.submit(mail);
    }

    /**
     * @category GETSET
     */
    public void setList(MailingList list) {
        this.list = list;
    }

    /**
     * @category GETSET
     */
    public MailingList getList() {
        return list;
    }

    @Override
    public String toString() {
        return "ListDestination [list=" + list.getAddress() + "]";
    }
}
