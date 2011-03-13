package mireka.forward;

import mireka.destination.MailDestination;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

/**
 * A ForwardDestination assigned to a recipient indicates that the mail should
 * be redistributed to multiple recipients, without changing the reverse path.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-3.9">RFC 5321 3.9
 *      Mailing Lists and Aliases</a>
 */
public class ForwardDestination implements MailDestination {
    private ForwardList list;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        list.submit(mail);
    }

    /**
     * @category GETSET
     */
    public void setList(ForwardList list) {
        this.list = list;
    }

    /**
     * @category GETSET
     */
    public ForwardList getList() {
        return list;
    }

    @Override
    public String toString() {
        return "ForwardDestination [list=" + list.getAddress() + "]";
    }

}
