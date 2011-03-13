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
     * @category GETSET
     */
    public void setReversePath(String reversePath) {
        this.reversePath = reversePath;
    }

    /**
     * @category GETSET
     */
    public String getReversePath() {
        return reversePath;
    }

    /**
     * @category GETSET
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * @category GETSET
     */
    public Destination getDestination() {
        return destination;
    }

}
