package mireka.transmission.immediate;

import mireka.smtp.EnhancedStatus;

/**
 * Indicates that the specific host, must not be connected at this time, because
 * there are too many open connections to it or it recently failed. It can also
 * indicate that all hosts of a domain has such problems. In such cases the
 * transmission must be postponed. Note that this does not count as a retry in
 * SMTP terms, because the connection have not even attempted.
 */
public class PostponeException extends Exception {
    private static final long serialVersionUID = 9171565056859085240L;
    private final int recommendedDelay;
    private final EnhancedStatus enhancedStatus;
    private RemoteMta remoteMta;

    /**
     * Construct a new exception without a remote MTA, which must be set later.
     */
    public PostponeException(int recommendedDelay,
            EnhancedStatus enhancedStatus, String message) {
        super(message);
        this.recommendedDelay = recommendedDelay;
        this.enhancedStatus = enhancedStatus;
    }

    /**
     * Returns the recommended delay until the host will likely become
     * available, in seconds.
     */
    public int getRecommendedDelay() {
        return recommendedDelay;
    }

    /**
     * Returns the enhanced status which should be reported if no more
     * postponing is possible.
     */
    public EnhancedStatus getEnhancedStatus() {
        return enhancedStatus;
    }

    /**
     * Sets the remote MTA which will be returned by {@link #getRemoteMta()}
     */
    public void setRemoteMta(RemoteMta remoteMta) {
        this.remoteMta = remoteMta;
    }

    /**
     * Returns the remote MTA for which the connection must be postponed, or one
     * of the MTA hosts if there are more.
     */
    public RemoteMta getRemoteMta() {
        return remoteMta;
    }

}
