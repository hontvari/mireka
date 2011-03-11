package mireka.list;

/**
 * Member holds information about a mailing list member.
 */
public class ListMember extends mireka.forward.Member {
    /**
     * The member does not get mails from the list, but he still can post mails.
     */
    private boolean noDelivery;

    /**
     * @category GETSET
     */
    public void setNoDelivery(boolean noDelivery) {
        this.noDelivery = noDelivery;
    }

    /**
     * @category GETSET
     */
    public boolean isNoDelivery() {
        return noDelivery;
    }

}
