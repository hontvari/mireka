package mireka.destination;


/**
 * ResponsibleDestination is a Destination which takes complete responsibility
 * for the delivery of the mail.
 */
public interface ResponsibleDestination extends Destination {
    /**
     * Returns true if the other object specifies the same destination. If
     * several recipients has the same destination, then all those recipients
     * are sent to a single destination object.
     */
    public boolean equals(Object obj);

    /**
     * Returns the hash code value for the object, conforming to the
     * {@link #equals(Object)} function.
     */
    public int hashCode();

    /**
     * Returns a string representation of the destination suitable for logging
     * purposes.
     */
    public String toString();
}
