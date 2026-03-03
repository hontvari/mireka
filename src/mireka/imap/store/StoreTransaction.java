package mireka.imap.store;

import mireka.imap.UnavailableException;

public interface StoreTransaction extends AutoCloseable {
    void commit() throws UnavailableException;

    @Override
    void close();
}
