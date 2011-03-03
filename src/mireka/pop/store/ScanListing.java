package mireka.pop.store;

/**
 * ScanListing represents a reply line sent in response to a POP3 LIST command.
 */
public class ScanListing {
    public final int id;
    public final long length;

    public ScanListing(int id, long length) {
        this.id = id;
        this.length = length;
    }

    @Override
    public String toString() {
        return id + " " + length;
    }

}
