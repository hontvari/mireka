package mireka.imap.store;

import java.io.InputStream;
import java.time.Instant;

import mireka.imap.MessageFlagSet;
import mireka.imap.UnavailableException;
import mireka.imap.update.UnilateralResponseOption;
import mireka.maildata.Maildata;
import mireka.maildata.io.Range;

public interface Mail {
    long uid();

    long seq();

    long charsize();

    /**
     * Returns a readonly set of message flags. 
     */
    MessageFlagSet flags();

    /**
     * Replaces the current set of message flags.
     * 
     * @return true if the set is changed
     */
    boolean setFlags(MessageFlagSet flag, UnilateralResponseOption option)
            throws UnavailableException;

    Instant date();

    InputStream body() throws UnavailableException;

    InputStream body(Range range) throws UnavailableException;

    Maildata maildata() throws UnavailableException;

}
