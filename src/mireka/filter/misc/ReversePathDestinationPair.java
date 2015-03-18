package mireka.filter.misc;

import mireka.destination.Destination;

/**
 * A reverse path - destination mapping, which is used by the
 * {@link RedirectPostmasterMail} filter.
 */
public class ReversePathDestinationPair {
    private String reversePath;
    private Destination destination;

    /**
     * @x.category GETSET
     */
    public void setReversePath(String reversePath) {
        this.reversePath = reversePath;
    }

    /**
     * @x.category GETSET
     */
    public String getReversePath() {
        return reversePath;
    }

    /**
     * @x.category GETSET
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * @x.category GETSET
     */
    public Destination getDestination() {
        return destination;
    }

}
