package mireka.transmission.queue;

import mireka.transmission.LocalMailSystemException;

/**
 * A MailProcessor object is responsible to process the mail which has come to the
 * head of a queue. It is created by a {@link MailProcessorFactory}, which
 * received the mail object for which this instance will be responsible.
 */
public interface MailProcessor {

    /**
     * It is called to process the mail for which this instance is responsible.
     * If this function returns without throwing an exception, then the queue
     * deletes the mail.
     * <p>
     * Note: this function is allowed to throw an exception only if there is a
     * <b>local</b> error. If, for example, a remote mail system cannot be
     * connected, then it must not throw an exception. Of course, appropriate
     * measures must be taken, for example by moving the mail into a retry
     * queue, but the processing must be considered successful.
     * 
     * @throws LocalMailSystemException
     *             if it cannot process its mail. The future of the mail depends
     *             on the {@link LocalMailSystemException#errorStatus()} of the
     *             thrown exception. If the exception indicates a transient
     *             error, then the mail remains in the queue, and a new
     *             MailProcessor object will be created and run 5 minutes later.
     *             This will be repeated up to 24 hours. If the exception
     *             indicates a permanent error then the queue will attempt to
     *             move the mail into an error folder, where the mail system
     *             administrator can examine it later.
     */
    void run() throws LocalMailSystemException;

}
