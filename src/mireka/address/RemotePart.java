package mireka.address;

/**
 * RemotePart identifies the destination system in a mail address. For example
 * in the john@example.com address example.com is the remote part.
 */
public interface RemotePart {
    public boolean equals(Object obj);

    public int hashCode();

}
