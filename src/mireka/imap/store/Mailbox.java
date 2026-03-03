package mireka.imap.store;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import mireka.imap.CompletionException;
import mireka.imap.MailId;
import mireka.imap.MessageFlagSet;
import mireka.imap.NonExistentException;
import mireka.imap.SequenceSet;
import mireka.imap.Session;
import mireka.imap.update.UnilateralResponseOption;

public interface Mailbox {

    /**
     * Client connections which selects this mailbox must register themself with this mailbox, so
     * they get further status notifications, for example when a mail is appended to the mailbox.
     * 
     * @throws NonExistentException if the mailbox is being deleted
     */
    void addSession(Session s) throws NonExistentException;

    void removeSession(Session s);

    default List<String> getFlags() {
        return List.of("\\Answered", "\\Flagged", "\\Deleted", "\\Seen", "\\Draft", "$Forwarded",
                "$MDNSent", "$Junk", "$NotJunk", "$Phishing");
    }

    List<String> attributes();

    Status status() throws CompletionException;

    MailId append(MessageFlagSet flags, @Nullable Instant date, InputStream in)
            throws CompletionException;

    /**
     * Returns the mails specified in the supplied {@link SequenceSet} in uid order.
     */
    MailIterator list(SequenceSet seqences) throws CompletionException;

    void expunge(UnilateralResponseOption option) throws CompletionException;

    void expunge(SequenceSet sequences, UnilateralResponseOption option) throws CompletionException;

    /**
     * Deletes the mailbox and its content but does not remove its entry from the
     * {@link Repository}.
     * 
     * @param initiator the IMAP session which initiated the deletion. Connections in which a
     * session with the specified mailbox is active will be disconnected.
     */
    void delete(Session initiator) throws CompletionException;

    static class Status {
        /**
         * The mailbox name from the viewpoint of the authenticated user.
         */
        public StoreMailboxName storeName;
        public long uidnext;
        public long uidvalidity;
        public long count;
        public long lastUid;
    }
}
