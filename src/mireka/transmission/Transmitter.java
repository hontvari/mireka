package mireka.transmission;

/**
 * A reliable (as specified by SMTP) mail transmitting service. If it accepts
 * the mail, i.e. it doesn't throw an exception immediately, then it also
 * accepts the responsibility to deliver the mail. If necessary it periodically
 * retries. If it gives up, then it creates and sends a Delivery Status
 * Notification message to the sender.
 */
public interface Transmitter {
    /**
     * Sends mail asynchronously. It returns immediately. If necessary it queues
     * the mail and retries periodically.
     * 
     * @throws LocalMailSystemException
     *             if it cannot accept the mail, e.g. because the disk is full.
     */
    void transmit(Mail mail) throws LocalMailSystemException;
}
