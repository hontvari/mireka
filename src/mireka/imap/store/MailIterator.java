package mireka.imap.store;

import mireka.imap.UnavailableException;

public interface MailIterator extends AutoCloseable {
    /**
     * Returns null at the end of the iteration.
     */
    Mail next() throws UnavailableException;

    @Override
    void close() throws UnavailableException;

    static MailIterator emptyIterator() {
        return new MailIterator() {
            
            @Override
            public Mail next() {
                return null;
            }

            @Override
            public void close() {
            }
        };
    }
}
