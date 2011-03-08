package mireka.forward;

import mireka.filter.Destination;

/**
 * A ForwardDestination assigned to a recipient indicates that the mail should
 * be redistributed to multiple recipients, without changing the reverse path.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-3.9">RFC 5321 3.9
 *      Mailing Lists and Aliases</a>
 */
public class ForwardDestination implements Destination {
    private ForwardList list;

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

}
