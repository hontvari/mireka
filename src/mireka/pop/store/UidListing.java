package mireka.pop.store;

/**
 * UidListing represents a reply line sent in response to the UIDL POP3 command.
 */
public class UidListing {
    public final int number;
    public final long uid;

    public UidListing(int number, long uid) {
        this.number = number;
        this.uid = uid;
    }

    @Override
    public String toString() {
        return number + " " + uid;
    }

}
