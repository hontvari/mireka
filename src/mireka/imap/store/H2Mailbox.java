package mireka.imap.store;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CiString;
import mireka.imap.CompletionException;
import mireka.imap.MailId;
import mireka.imap.MessageFlagSet;
import mireka.imap.NonExistentException;
import mireka.imap.SequenceSet;
import mireka.imap.Session;
import mireka.imap.UnavailableException;
import mireka.imap.parser.SeqRange;
import mireka.imap.update.ExpungeUpdate;
import mireka.imap.update.MessageCountUpdate;
import mireka.imap.update.MessageFlagUpdate;
import mireka.imap.update.UnilateralResponseOption;
import mireka.imap.update.Update;
import mireka.maildata.Maildata;
import mireka.maildata.io.MaildataInputStream;
import mireka.maildata.io.MaildataReadException;
import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.Range;
import mireka.maildata.io.Subsource;

/**
 * In order to avoid deadlock, first repository must be locked.
 */
public class H2Mailbox implements Mailbox {
    private static final long MAX_UNSIGNED_INT = 4_294_967_295L;
    private static final Logger logger = LoggerFactory.getLogger(H2Mailbox.class);

    /**
     * canonical name as used in the database. The authenticated user uses a different name,
     * specifically in its personal namespace. Many user has an INBOX mailbox.
     */
    String name;
    private long uidvalidity;
    private long uidnext;
    /** -1 if the mailbox is empty */
    private long lastuid;
    private long count;

    @GuardedBy("this")
    private final Set<Session> sessions = new HashSet<>();
    private Instant lastConsistencyCheck;
    /**
     * true if the mailbox is being deleted
     */
    private boolean deletion;

    @Override
    public synchronized List<String> attributes() {
        List<String> r = new ArrayList<>();
        return r;
    }

    @Override
    public synchronized Status status() throws mireka.imap.CompletionException {
        Status s = new Status();
        s.storeName = new StoreMailboxName(name);
        s.uidvalidity = uidvalidity;
        s.uidnext = uidnext;
        s.count = count;
        s.lastUid = lastuid;
        checkConsistency();
        return s;
    }

    private void checkConsistency() throws CorruptionException, UnavailableException {
        if (lastConsistencyCheck != null
                && lastConsistencyCheck.isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            return;
        try {
            String sql = "SELECT count(*) FROM mail WHERE mailbox = ?";
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            rs.next();
            long actualCount = rs.getLong(1);
            if (actualCount != count)
                throw new CorruptionException(
                        "Actual count of mails in mailbox does not match. Mailbox: " + name
                                + ", actual count: " + actualCount + ", stored count: " + count);

            sql = "SELECT MAX(uid) FROM mail WHERE mailbox = ?";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            rs = st.executeQuery();
            rs.next();
            long actualLastUid = rs.getLong(1);
            if (rs.wasNull())
                actualLastUid = -1;
            if (actualLastUid != lastuid)
                throw new CorruptionException("Actual last uid in mailbox does not match. Mailbox: "
                        + name + ", actual mail with last uid: " + actualLastUid
                        + ", stored uid in mailbox: " + lastuid);
            lastConsistencyCheck = Instant.now();
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    /**
     * Appends a new mail to the end of this mailbox.
     */
    @Override
    public synchronized MailId append(MessageFlagSet flags, @Nullable Instant date, InputStream in)
            throws CompletionException {
        checkConsistency();
        try {
            String sql = "INSERT INTO mail (mailbox, uid, seq, date, body) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            if (uidnext >= MAX_UNSIGNED_INT)
                throw new RuntimeException();
            long uid = uidnext;
            long seq = count + 1;
            st.setLong(2, uid);
            st.setLong(3, seq);
            st.setTimestamp(4, Timestamp.from(date == null ? Instant.now() : date));
            st.setBinaryStream(5, in);
            st.executeUpdate();
            sql = "UPDATE mailbox SET uidnext = ?, lastuid = ?, count = ? WHERE name = ?";
            st = H2Session.connection().prepareStatement(sql);
            st.setLong(1, uidnext);
            st.setLong(2, uid);
            st.setLong(3, count + 1);
            st.setString(4, name);
            st.executeUpdate();
            sql = "INSERT INTO flag (mailbox, uid, name) VALUES (?, ?, ?)";
            st = H2Session.connection().prepareStatement(sql);
            for (CiString flagName : flags) {
                st.setString(1, name);
                st.setLong(2, uid);
                st.setString(3, flagName.toString());
                st.executeUpdate();
            }
            sql = "SELECT body FROM mail WHERE mailbox = ? AND uid = ?";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            st.setLong(2, uid);
            ResultSet rs = st.executeQuery();
            if (!rs.next())
                throw new RuntimeException();
            long length = rs.getBlob("body").length();
            // System.out.println("Blob: " + length);
            // System.out.println(Arrays.toString(rs.getBlob("body").getBytes(0, (int) length)));
            sql = "UPDATE mail SET charsize = ? WHERE mailbox = ? AND uid = ?";
            st = H2Session.connection().prepareStatement(sql);
            st.setLong(1, length);
            st.setString(2, name);
            st.setLong(3, uid);
            st.executeUpdate();
            final MessageCountUpdate update = new MessageCountUpdate(count + 1);
            Transaction.runAfterCommit(() -> queueUpdate(update));
            lastuid = uid;
            uidnext++;
            count++;
            logger.debug("Message {}, uid {} is added to mailbox {}",
                    new Object[] { seq, uid, name });
            return new MailId(uidvalidity, uid);
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    @Override
    public synchronized void addSession(Session s) throws NonExistentException {
        if (deletion)
            throw new NonExistentException();
        sessions.add(s);
    }

    @Override
    public synchronized void removeSession(Session s) {
        sessions.remove(s);
        notify();
    }

    private synchronized void queueUpdate(Update update) {
        for (Session session : sessions) {
            session.commands.sendOrQueue(update);
        }
    }

    /**
     * @param sequences last result element must be already expanded
     */
    @Override
    public synchronized MailIterator list(SequenceSet sequences) throws UnavailableException {
        try {
            if (sequences.ranges.isEmpty())
                return MailIterator.emptyIterator();
            SequenceSet.AsteriskSource as = new SequenceSet.AsteriskSource();
            as.count = count;
            as.uidnext = uidnext;
            as.lastuid = lastuid;
            logger.trace("AsteriskSource: {}", as);
            sequences.normalize(as);
            logger.trace("Normalized sequences {}", sequences);

            if (sequences.ranges.size() == 1) {
                SeqRange range = sequences.ranges.get(0);
                if (range.isNumber()) {
                    if (sequences.uid)
                        return listByUid(range.begin);
                    else
                        return listBySeq(range.begin);
                } else {
                    if (sequences.uid)
                        return listByUid(range);
                    else
                        return listBySeq(range);
                }
            } else {
                if (sequences.uid)
                    return listByMoreUidRange(sequences);
                else
                    return listByMoreSeqRange(sequences);
            }
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    private MailIterator listBySeq(long begin) {
        // TODO Auto-generated method stub
        return null;
    }

    private MailIterator listBySeq(SeqRange range) {
        // TODO Auto-generated method stub
        return null;
    }

    private MailIterator listByMoreSeqRange(SequenceSet sequences) {
        // TODO Auto-generated method stub
        return null;
    }

    private MailIterator listByUid(long uid) throws UnavailableException, SQLException {
        String sql = "SELECT uid, seq, charsize, date, body FROM mail WHERE mailbox = ? AND uid = ?";
        PreparedStatement st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        st.setLong(2, uid);
        ResultSet rs = st.executeQuery();
        sql = "SELECT uid, name FROM flag WHERE mailbox = ? AND uid = ?";
        st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        st.setLong(2, uid);
        ResultSet flags = st.executeQuery();
        return new ResultsetMailIterator(rs, flags);
    }

    /**
     * @param r normalized
     */
    private MailIterator listByUid(SeqRange r) throws UnavailableException, SQLException {
        String sql = "SELECT uid, seq, charsize, date, body "
                + "FROM mail WHERE mailbox = ? AND uid BETWEEN ? AND ? ORDER BY uid";
        PreparedStatement st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        st.setLong(2, r.begin);
        st.setLong(3, r.end);
        ResultSet rs = st.executeQuery();
        sql = "SELECT uid, name FROM flag WHERE mailbox = ? AND uid BETWEEN ? AND ? "
                + "ORDER BY uid";
        st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        st.setLong(2, r.begin);
        st.setLong(3, r.end);
        ResultSet flags = st.executeQuery();
        return new ResultsetMailIterator(rs, flags);
    }

    @SuppressWarnings("resource")
    private MailIterator listByMoreUidRange(SequenceSet sequences)
            throws UnavailableException, SQLException {
        String sql = "CREATE LOCAL TEMPORARY TABLE tmp_selected_id (id BIGINT PRIMARY KEY) TRANSACTIONAL";
        PreparedStatement st = H2Session.connection().prepareStatement(sql);
        st.executeUpdate();
        try {
            sql = "MERGE INTO tmp_selected_id SELECT uid FROM mail WHERE mailbox = ? "
                    + "AND uid BETWEEN ? AND ?";
            st = H2Session.connection().prepareStatement(sql);
            for (SeqRange r : sequences.ranges) {
                st.setString(1, name);
                st.setLong(2, r.begin);
                st.setLong(3, r.end);
                st.executeUpdate();
            }
            sql = "SELECT uid, seq, charsize, date, body FROM mail WHERE mailbox = ? "
                    + "AND uid IN (SELECT id FROM tmp_selected_id) ORDER BY uid";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            sql = "SELECT uid, name FROM flag WHERE mailbox = ? "
                    + "AND uid IN (SELECT id FROM tmp_selected_id) ORDER BY uid";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            ResultSet flags = st.executeQuery();
            return new ResultsetMailIterator(rs, flags);
        } finally {
            sql = "DROP TABLE tmp_selected_id";
            st = H2Session.connection().prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void uidSequenceSetToTable(SequenceSet sequences)
            throws UnavailableException, SQLException {
        String sql = "CREATE LOCAL TEMPORARY TABLE tmp_selected_id (id BIGINT PRIMARY KEY) TRANSACTIONAL";
        PreparedStatement st = H2Session.connection().prepareStatement(sql);
        st.executeUpdate();

        sql = "MERGE INTO tmp_selected_id SELECT uid FROM mail WHERE mailbox = ? "
                + "AND uid BETWEEN ? AND ?";
        st = H2Session.connection().prepareStatement(sql);
        for (SeqRange r : sequences.ranges) {
            st.setString(1, name);
            st.setLong(2, r.begin);
            st.setLong(3, r.end);
            st.executeUpdate();
        }
    }

    private void dropTempTables(String... tables) throws UnavailableException {
        try {
            String tablesString = String.join(", ", tables);
            String sql = "DROP TABLE IF EXISTS " + tablesString;
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st = H2Session.connection().prepareStatement(sql);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    @Override
    public synchronized void expunge(UnilateralResponseOption option) throws UnavailableException {
        try {
            String sql = "CREATE LOCAL TEMPORARY TABLE tmp_expunge "
                    + "(uid BIGINT PRIMARY KEY, seq BIGINT) TRANSACTIONAL";
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st.executeUpdate();

            sql = "MERGE INTO tmp_expunge SELECT m.uid, m.seq FROM mail m "
                    + "JOIN flag f ON m.mailbox = f.mailbox AND m.uid = f.uid "
                    + "WHERE m.mailbox = ? and f.name = '\\Deleted'";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            st.executeLargeUpdate();

            expungeUsingTable(option);
        } catch (SQLException e) {
            throw new UnavailableException(e);
        } finally {
            dropTempTables("tmp_expunge");
        }
    }

    @Override
    public synchronized void expunge(SequenceSet sequences, UnilateralResponseOption option)
            throws UnavailableException {
        try {
            uidSequenceSetToTable(sequences);

            String sql = "CREATE LOCAL TEMPORARY TABLE tmp_expunge (uid BIGINT PRIMARY KEY, seq BIGINT) TRANSACTIONAL";
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st.executeUpdate();

            sql = "MERGE INTO tmp_expunge SELECT m.uid, m.seq FROM mail m "
                    + "JOIN flag f ON m.mailbox = f.mailbox AND m.uid = f.uid "
                    + "JOIN tmp_selected_id s ON m.uid = s.id "
                    + "WHERE m.mailbox = ? and f.name = '\\Deleted'";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            st.executeLargeUpdate();

            expungeUsingTable(option);
        } catch (SQLException e) {
            throw new UnavailableException(e);
        } finally {
            dropTempTables("tmp_selected_id", "tmp_expunge");
        }

    }

    /**
     * expunge messages listed in the tmp_expunge table
     */
    private void expungeUsingTable(UnilateralResponseOption option)
            throws SQLException, UnavailableException {
        String sql = "SELECT seq FROM tmp_expunge ORDER BY seq DESC";
        PreparedStatement st = H2Session.connection().prepareStatement(sql);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            long seq = rs.getLong(1);
            logger.trace("delete mail {} from {}", seq, name);
            ExpungeUpdate update = new ExpungeUpdate();
            update.seq = seq;
            update.option = option;
            queueUpdate(update);
        }
        st.close();

        sql = "DELETE FROM mail WHERE mailbox = ? AND uid IN (SELECT uid FROM tmp_expunge)";
        st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        st.executeUpdate();

        sql = "DELETE FROM flag WHERE mailbox = ? AND uid IN (SELECT uid FROM tmp_expunge)";
        st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        st.executeUpdate();

        sql = "UPDATE mail SET seq = rownum() WHERE mailbox = ? ORDER BY uid";
        st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        st.executeUpdate();

        sql = "SELECT COUNT(*) AS count, MAX(uid) AS lastuid FROM mail WHERE mailbox = ?";
        st = H2Session.connection().prepareStatement(sql);
        st.setString(1, name);
        rs = st.executeQuery();
        rs.next();
        count = rs.getLong("count");
        lastuid = count == 0 ? -1 : rs.getLong("lastuid");
        st.close();

        sql = "UPDATE mailbox SET lastuid = ?, count = ? WHERE name = ?";
        st = H2Session.connection().prepareStatement(sql);
        st.setLong(1, lastuid);
        st.setLong(2, count);
        st.setString(3, name);
        st.executeUpdate();
    }

    @Override
    public synchronized void delete(Session initiator) throws UnavailableException {
        try {
            deletion = true;
            for (Session session : sessions) {
                if (session != initiator)
                    session.connection.shutdownNow("The selected mailbox is being deleted");
            }

            String sql = "DELETE FROM flag WHERE mailbox = ?";
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            st.executeUpdate();

            sql = "DELETE FROM mail WHERE mailbox = ?";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            st.executeUpdate();

            sql = "DELETE FROM mailbox WHERE name = ?";
            st = H2Session.connection().prepareStatement(sql);
            st.setString(1, name);
            st.executeUpdate();

        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    public static void load(ResultSet rs, H2Mailbox m) throws SQLException {
        m.name = rs.getString("name");
        m.uidvalidity = rs.getLong("uidvalidity");
        m.uidnext = rs.getLong("uidnext");
        m.lastuid = rs.getLong("lastuid");
        m.count = rs.getLong("count");
    }

    public static H2Mailbox create(StoreMailboxName name, DataSource ds) {
        H2Mailbox m = new H2Mailbox();
        m.name = name.toString();
        m.uidvalidity = System.currentTimeMillis() / 1000;
        m.uidnext = 1;
        m.lastuid = -1;
        m.count = 0;
        return m;
    }

    public static void insert(H2Mailbox mailbox) throws UnavailableException {
        try {
            String s = "INSERT INTO mailbox (name, uidvalidity, uidnext, lastuid, count) "
                    + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement st = H2Session.connection().prepareStatement(s);
            st.setString(1, mailbox.name);
            st.setLong(2, mailbox.uidvalidity);
            st.setLong(3, mailbox.uidnext);
            st.setLong(4, mailbox.lastuid);
            st.setLong(5, mailbox.count);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    private class ResultsetMailIterator implements MailIterator {
        private ResultSet mails;
        private ResultSet flags;
        private boolean flagsEof;

        public ResultsetMailIterator(ResultSet mails, ResultSet flags) throws UnavailableException {
            try {
                this.mails = mails;
                this.flags = flags;
                flagsEof = !flags.next();
            } catch (SQLException e) {
                throw new UnavailableException(e);
            }
        }

        @Override
        public Mail next() throws UnavailableException {
            try {
                if (!mails.next())
                    return null;
                H2Mail mail = new H2Mail();
                mail.mailbox = name;
                mail.uid = mails.getLong("uid");
                mail.seq = mails.getLong("seq");
                mail.charsize = mails.getLong("charsize");
                mail.date = mails.getTimestamp("date").toInstant();
                mail.flags = readFlags(mail.uid);
                mail.body = mails.getBlob("body");
                return mail;
            } catch (SQLException e) {
                throw new UnavailableException(e);
            }
        }

        private MessageFlagSet readFlags(long uid) throws SQLException {
            MessageFlagSet r = new MessageFlagSet();
            if (flagsEof)
                return r;
            long flagUid = flags.getLong("uid");
            while (!flagsEof && flagUid < uid) {
                logger.error("No mail record for flag record, uid {}", flagUid);
                flagsEof = !flags.next();
                if (!flagsEof)
                    flagUid = flags.getLong("uid");
            }
            while (!flagsEof && flagUid == uid) {
                r.add(flags.getString("name"));
                flagsEof = !flags.next();
                if (!flagsEof)
                    flagUid = flags.getLong("uid");
            }
            return r;
        }

        @Override
        public void close() throws UnavailableException {
            try {
                mails.close();
                flags.close();
            } catch (SQLException e) {
                throw new UnavailableException(e);
            }
        }
    }

    private class H2Mail implements Mail {
        private final Logger logger = LoggerFactory.getLogger(H2Mailbox.H2Mail.class);
        private String mailbox;
        private long uid;
        private long seq;
        private long charsize;
        private Blob body;
        private MessageFlagSet flags = new MessageFlagSet();
        private Instant date;
        private Maildata maildata;

        @Override
        public long uid() {
            return uid;
        }

        @Override
        public long seq() {
            return seq;
        }

        @Override
        public long charsize() {
            return charsize;
        }

        @Override
        public MessageFlagSet flags() {
            return flags;
        }

        @Override
        public boolean setFlags(MessageFlagSet newFlags, UnilateralResponseOption option)
                throws UnavailableException {
            try {
                if (newFlags.equals(flags))
                    return false;
                String s = "DELETE flag WHERE mailbox=? AND uid=? AND name=?";
                PreparedStatement deleteSt = H2Session.connection().prepareStatement(s);
                s = "INSERT INTO flag (mailbox, uid, name) VALUES (?, ?, ?)";
                PreparedStatement insertSt = H2Session.connection().prepareStatement(s);

                for (CiString flag : flags) {
                    if (!newFlags.contains(flag)) {
                        deleteSt.setString(1, mailbox);
                        deleteSt.setLong(2, uid);
                        deleteSt.setString(3, flag.toString());
                        deleteSt.executeUpdate();
                        logger.trace("Delete flag: {} {} {}", new Object[] { mailbox, uid, flag });
                    }
                }
                for (CiString flag : newFlags) {
                    if (!flags.contains(flag)) {
                        insertSt.setString(1, mailbox);
                        insertSt.setLong(2, uid);
                        insertSt.setString(3, flag.toString());
                        insertSt.executeUpdate();
                        logger.trace("Insert flag: {} {} {}", new Object[] { mailbox, uid, flag });
                    }
                }
                flags = newFlags;
                MessageFlagUpdate update = new MessageFlagUpdate();
                update.uid = uid;
                update.seq = seq;
                update.flags = flags;
                update.option = option;
                queueUpdate(update);
                return true;
            } catch (SQLException e) {
                throw new UnavailableException(e);
            }
        }

        @Override
        public Instant date() {
            return date;
        }

        @Override
        public InputStream body() throws UnavailableException {
            try {
                return body.getBinaryStream();
            } catch (SQLException e) {
                throw new UnavailableException(e);
            }
        }

        @Override
        public InputStream body(Range range) throws UnavailableException {
            try {
                return body.getBinaryStream(range.start + 1, range.length);
            } catch (SQLException e) {
                throw new UnavailableException(e);
            }
        }

        @Override
        public Maildata maildata() throws UnavailableException {
            if (maildata == null) {
                BlobMaildataSource maildataSource = new BlobMaildataSource(body);
                maildata = new Maildata(maildataSource);
            }
            return maildata;
        }

    }

    private static class BlobMaildataSource implements MaildataSource {
        private Blob blob;

        BlobMaildataSource(Blob blob) {
            this.blob = blob;
        }

        @Override
        public MaildataInputStream getInputStream(Range range) throws MaildataReadException {
            try {
                return new MaildataInputStream(new Subsource(this, range),
                        blob.getBinaryStream(range.start + 1, range.length));
            } catch (SQLException e) {
                throw new MaildataReadException(e);
            }
        }

        @Override
        public long length() throws MaildataReadException {
            try {
                return blob.length();
            } catch (SQLException e) {
                throw new MaildataReadException(e);
            }
        }

        @Override
        public void close() {
        }
    }
}
