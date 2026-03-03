package mireka.imap.store;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CompletionException;
import mireka.imap.MailboxFlag;
import mireka.imap.Namespace;
import mireka.imap.NonExistentException;
import mireka.imap.SettingsRepo;
import mireka.imap.UnavailableException;
import mireka.imap.acl.AccessControl;
import mireka.imap.acl.Right;
import mireka.imap.server.ServerThread;
import mireka.imap.update.MailboxDeletionUpdate;
import mireka.imap.update.UnilateralResponseOption;
import mireka.login.User;
import mireka.util.AssertionException;

public class H2Repository implements Repository {
    private final Logger logger = LoggerFactory.getLogger(H2Repository.class);
    private String url;
    private AccessControl accessControl;
    private SettingsRepo settingsRepo;
    private Server console;
    private JdbcDataSource ds;
    @GuardedBy("this")
    private final Map<StoreMailboxName, Mailbox> mailboxes = new HashMap<>();
    @GuardedBy("this")
    private final Map<User, Set<MailboxName>> subscriptions = new HashMap<>();
    /**
     * Stores special-use flags. It is assumed that one (user, mailbox) combination has at most one
     * special-use flag.
     */
    @GuardedBy("this")
    private final Map<MailboxFlagKey, MailboxFlag> flags = new HashMap<>();
    @GuardedBy("this")
    private final List<ServerThread> serverThreads = new ArrayList<>();

    @PostConstruct
    public void start() throws SQLException, UnavailableException {
        logger.info("Starting H2 IMAP Repository {}...", url);
        ds = new JdbcDataSource();
        ds.setUrl(url);
        console = org.h2.tools.Server.createWebServer().start();

        H2Session.init(ds);
        try {
            upgradeDb();
            loadFromDb();
        } finally {
            H2Session.cleanup();
        }
    }

    private void upgradeDb() throws SQLException, UnavailableException {
        Statement st = H2Session.connection().createStatement();
        boolean isEmpty = true;
        int version = 0;
        ResultSet rs = st.executeQuery("SHOW TABLES");
        while (rs.next()) {
            if (rs.getString(1).equalsIgnoreCase("version")) {
                isEmpty = false;
                break;
            }
        }
        if (!isEmpty) {
            st = H2Session.connection().createStatement();
            rs = st.executeQuery("SELECT * FROM version");
            rs.next();
            version = rs.getInt(1);
        }

        if (isEmpty) {
            st.executeUpdate("CREATE TABLE version (version INT)");
            st.executeUpdate("INSERT INTO version VALUES (1)");
            st.executeUpdate("CREATE TABLE mailbox "
                    + "(name VARCHAR PRIMARY KEY, uidvalidity BIGINT, uidnext BIGINT, "
                    + "lastuid BIGINT, count BIGINT)");
            // Stores special-use flags, it is assumed that one (user, mailbox) combination has
            // only one special-use flag
            st.executeUpdate(
                    "CREATE TABLE mailbox_flag (mailbox VARCHAR, username VARCHAR, flag VARCHAR, PRIMARY KEY(mailbox, username))");
            st.executeUpdate("CREATE TABLE mail (mailbox VARCHAR, uid BIGINT, seq BIGINT, "
                    + "charsize BIGINT, date TIMESTAMP WITH TIME ZONE NOT NULL, "
                    + "body BINARY LARGE OBJECT, PRIMARY KEY (mailbox, uid), "
                    + "CONSTRAINT unique_seq UNIQUE (mailbox, seq))");
            st.executeUpdate(
                    "CREATE TABLE flag (mailbox VARCHAR, uid BIGINT, name VARCHAR, PRIMARY KEY (mailbox, uid, name))");
            st.executeUpdate(
                    "CREATE TABLE subscription (username VARCHAR, mailbox VARCHAR, PRIMARY KEY (username, mailbox))");
            st.executeUpdate("INSERT INTO mailbox VALUES ('#shared/sysadm', 1, 1, -1, 0)");
            st.executeUpdate("INSERT INTO mailbox VALUES ('#shared/sysadm/announce', 1, 1, -1, 0)");
            st.executeUpdate("INSERT INTO mailbox VALUES ('#shared/sakk', 1, 1, -1, 0)");
            st.executeUpdate("INSERT INTO subscription VALUES ('hontvari', '#shared/sysadm')");
            st.executeUpdate(
                    "INSERT INTO subscription VALUES ('hontvari', '#shared/sysadm/announce')");
            logger.info("H2 IMAP Repository {} db created", url);
        } else if (version < 1) {
            // upgrade
            logger.info("H2 IMAP Repository {} db upgraded", url);
        } else {
            logger.info("H2 IMAP Repository {} db structure is up-to-date", url);
        }
    }

    private void loadFromDb() throws UnavailableException, SQLException {
        String sql = "SELECT * FROM mailbox";
        PreparedStatement st = H2Session.connection().prepareStatement(sql);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            H2Mailbox m = new H2Mailbox();
            H2Mailbox.load(rs, m);
            mailboxes.put(new StoreMailboxName(m.name), m);
        }
        st.close();

        st = H2Session.connection().prepareStatement("SELECT * FROM subscription");
        rs = st.executeQuery();
        while (rs.next()) {
            User user = new User(rs.getString(1));
            MailboxName mbname = parseMailboxName(user, rs.getString(2));
            Set<MailboxName> mbnames = subscriptions.computeIfAbsent(user,
                    u -> new HashSet<MailboxName>());
            mbnames.add(mbname);
        }
        st.close();

        st = H2Session.connection().prepareStatement("SELECT * FROM mailbox_flag");
        rs = st.executeQuery();
        while (rs.next()) {
            User user = new User(rs.getString(1));
            StoreMailboxName storeMbname = new StoreMailboxName(rs.getString(2));
            MailboxFlag flag = MailboxFlag.valueOf(rs.getString(3));
            MailboxFlagKey key = new MailboxFlagKey(user, storeMbname);
            flags.put(key, flag);
        }
        st.close();
    }

    @PreDestroy
    public void shutdown() {
        if (console != null)
            console.stop();
        logger.info("H2 IMAP Repository {} stopped", url);
    }

    @Override
    public void begin() {
        logger.trace("begin");
        H2Session.init(ds);
        Transaction.setStoreTransaction(new StoreTransaction() {
            @Override
            public void commit() throws UnavailableException {
                H2Session.commit();
            }

            @Override
            public void close() {
                H2Session.cleanup();
            }
        });
    }

    /**
     * Does not do permission checks.
     * 
     * @return null if not exists
     */
    @Override
    public synchronized Mailbox queryMailbox(MailboxName name) throws UnavailableException {
        if (name.ownInOtherUsersNamespace)
            return null;
        return mailboxes.get(name.storeName);
    }

    @Override
    public synchronized void createDefaultMailboxes(User user) throws UnavailableException {
        if (!settingsRepo.get(user).hasPersonalNamespace())
            return;
        createDefaultMailbox(user, "INBOX", null);
        createDefaultMailbox(user, "Trash", MailboxFlag.R_TRASH);
        createDefaultMailbox(user, "Sent", MailboxFlag.R_SENT);
        createDefaultMailbox(user, "Drafts", MailboxFlag.R_DRAFTS);
        createDefaultMailbox(user, "Junk", MailboxFlag.R_JUNK);
        createDefaultMailbox(user, "Archives", MailboxFlag.R_ARCHIVE);
    }

    private void createDefaultMailbox(User user, String name, MailboxFlag flag)
            throws UnavailableException {
        MailboxName mbname = parseMailboxName(user, name);
        if (queryMailbox(mbname) == null) {
            createMailbox(mbname.storeName);
            subscribe(mbname);
            if (flag != null)
                setMailboxFlag(mbname, flag);
        }
    }

    @Override
    public synchronized void createMailbox(MailboxName name)
            throws AlreadyExistsException, CompletionException {
        if (name.ownInOtherUsersNamespace)
            throw new CompletionException(
                    "Cannot create your own mailboxes in the Other Users' namespace");
        if (queryMailbox(name) != null)
            throw new AlreadyExistsException("Mailbox already exists: " + name.original);

        createMailbox(name.storeName);
    }

    private void createMailbox(StoreMailboxName name) throws UnavailableException {
        H2Mailbox mailbox = H2Mailbox.create(name, ds);
        H2Mailbox.insert(mailbox);
        mailboxes.put(name, mailbox);
    }

    @Override
    public synchronized Mailbox removeMailbox(MailboxName mbname, UnilateralResponseOption option)
            throws NonExistentException, UnavailableException {
        Mailbox mailbox = queryMailbox(mbname);
        if (mailbox == null)
            throw new NonExistentException();
        mailboxes.remove(mbname.storeName);
        for (ServerThread serverThreads : serverThreads) {
            MailboxDeletionUpdate update = new MailboxDeletionUpdate();
            update.mailbox = mbname.storeName;
            update.option = option;
            serverThreads.connections.forEach(s -> s.commands.sendOrQueue(update));
        }
        return mailbox;
    }

    /**
     * Does not check for the existence or accessibility of the supplied mailbox.
     */
    @Override
    public synchronized void subscribe(MailboxName mbname) throws UnavailableException {
        try {
            String sql = "MERGE INTO subscription VALUES(?, ?)";
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st.setString(1, mbname.user.name());
            st.setString(2, mbname.name);
            st.executeUpdate();

            Set<MailboxName> set = subscriptions.computeIfAbsent(mbname.user,
                    u -> new HashSet<MailboxName>());
            set.add(mbname);
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    private void setMailboxFlag(MailboxName mbname, MailboxFlag flag) throws UnavailableException {
        try {
            String sql = "MERGE INTO mailbox_flag VALUES(?, ?, ?)";
            PreparedStatement st = H2Session.connection().prepareStatement(sql);
            st.setString(1, mbname.user.name());
            st.setString(2, mbname.storeName.name);
            st.setString(3, flag.imapName);
            st.executeUpdate();

            MailboxFlagKey key = new MailboxFlagKey(mbname.user, mbname.storeName);
            flags.put(key, flag);
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    @Override
    public synchronized MailboxHierarchy userMailboxHierarchy(User user, Namespace ns) {
        MailboxHierarchy h = new MailboxHierarchy(ns);
        for (StoreMailboxName sname : mailboxes.keySet()) {
            MailboxName mbname = parseStoreMailboxName(user, sname);
            if (ns.equals(mbname.namespace) && accessControl.hasAccess(mbname, Right.LOOKUP)) {
                Node node = h.createPath(mbname);
                node.isExpicit = true;
                node.mailbox = mailboxes.get(sname);
                if (node.mailbox != null) {
                    MailboxFlag flag = flags.get(new MailboxFlagKey(user, sname));
                    if (flag != null)
                        node.flags.add(flag);
                }
            }
        }
        if (logger.isTraceEnabled())
            logger.trace("Store mailbox names: " + mailboxes.keySet().toString());
        h.root.debugPrintTree();
        return h;
    }

    @Override
    public synchronized MailboxHierarchy subsribedMailboxHierarchy(User user, Namespace ns) {
        MailboxHierarchy h = new MailboxHierarchy(ns);
        Set<MailboxName> emptySet = Collections.emptySet();
        for (MailboxName mbname : subscriptions.getOrDefault(user, emptySet)) {
            if (ns.equals(mbname.namespace) && accessControl.hasAccess(mbname, Right.LOOKUP)) {
                Node node = h.createPath(mbname);
                node.isExpicit = true;
                node.mailbox = mailboxes.get(mbname.storeName);
            }
        }
        if (logger.isTraceEnabled())
            logger.trace("Subscribed mailbox names: " + subscriptions.get(user).toString());
        h.root.debugPrintTree();
        return h;
    }

    /**
     * Only partially implemented, as necessary for other functions!
     */
    @Override
    public MailboxName parseStoreMailboxName(User user, StoreMailboxName sname) {
        String snames = sname.name;
        MailboxName name = new MailboxName();
        name.storeName = sname;
        name.user = user;
        if (snames.equals("~")) {
            name.namespace = namespaces().others.get(0);
            name.path = "";
        } else if (snames.startsWith("~")) {
            int iSeparator = snames.indexOf('/');
            if (iSeparator == -1)
                name.owner = new User(snames.substring(1));
            else
                name.owner = new User(snames.substring(1, iSeparator));
            if (name.owner.equals(name.user)) {
                name.namespace = namespaces().personals.get(0);
                if (iSeparator == -1)
                    name.path = "";
                else
                    name.path = snames.substring(iSeparator + 1);
            } else {
                name.namespace = namespaces().others.get(0);
                name.path = snames.substring(1);
            }
        } else if (snames.startsWith("#shared/")) {
            name.namespace = namespaces().shareds.get(0);
            name.path = snames.substring("#shared/".length());
        } else {
            throw new AssertionException();
        }
        name.original = name.namespace.prefix + name.path;
        return name;
    }

    @Override
    public synchronized void addServerThread(ServerThread serverThread) {
        serverThreads.add(serverThread);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Inject
    public void setAccessControl(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    @Inject
    public void setSettingsRepo(SettingsRepo settingsRepo) {
        this.settingsRepo = settingsRepo;
    }

    private static class MailboxFlagKey {
        public final User user;
        public final StoreMailboxName mailbox;

        public MailboxFlagKey(User user, StoreMailboxName mailbox) {
            this.user = user;
            this.mailbox = mailbox;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mailbox, user);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MailboxFlagKey other = (MailboxFlagKey) obj;
            return Objects.equals(mailbox, other.mailbox) && Objects.equals(user, other.user);
        }
    }
}
