package mireka.pop.store;

import java.io.OutputStream;

import mireka.transmission.LocalMailSystemException;

/**
 * A <tt>MaildropAppender</tt> is used to add a new mail to a maildrop. It works
 * even if the maildrop is currently locked.
 */
public interface MaildropAppender {
    /**
     * Returns an output stream into which the mail must be written. Either
     * {@link #commit()} or {@link #rollback()} must be called later to close
     * this stream.
     */
    public OutputStream getOutputStream() throws LocalMailSystemException;

    /**
     * It closes the output stream, adds the mail to the maildrop and releases
     * the appender. Even if it throws an exception, the appender is always
     * released.
     * 
     * @throws LocalMailSystemException
     *             if the mail couldn't be added to the maildrop for some
     *             reason.
     */
    public void commit() throws LocalMailSystemException;

    /**
     * It closes the output stream if it is open, removes any temporary files
     * and releases the appender. It does not add the mail to the maildrop.
     */
    public void rollback();
}
