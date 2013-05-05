package mireka.transmission.immediate;

/**
 * Factory for {@link ImmediateSender}. 
 */
public interface ImmediateSenderFactory {
    /**
     * Returns a new instance of {@link ImmediateSender}.
     */
    ImmediateSender create();

    /**
     * Returns true if the created {@link ImmediateSender} requires that all 
     * recipients of the mail to be sent have the same remote-part.  
     */
    boolean singleDomainOnly();
}
