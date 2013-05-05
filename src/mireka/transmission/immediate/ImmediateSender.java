package mireka.transmission.immediate;

import mireka.transmission.Mail;

/**
 * An ImmediateSender makes a single, synchronous attempt to deliver mail to 
 * a remote system. 
 */
public interface ImmediateSender {
    /**
     * Synchronously transmits mail to a single domain.
     * 
     * @throws IllegalArgumentException
     *             if the domains of the recipients are not the same, or if the
     *             recipient is the special global postmaster address, which has
     *             no absolute domain.
     * @throws PostponeException
     *             if transmission to all of the hosts must be postponed,
     *             because all of them are assumed to be busy at this moment.
     */
    public void send(Mail mail) throws SendException,
            RecipientsWereRejectedException, IllegalArgumentException,
            PostponeException;
}