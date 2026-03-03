package mireka.imap;

import static mireka.imap.SessionState.SELECTED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mireka.imap.acl.AccessControl;
import mireka.imap.parser.Generator;
import mireka.imap.server.ConcurrentCommands;
import mireka.imap.server.ConcurrentConnections;
import mireka.imap.server.Connection;
import mireka.imap.server.ImapServer;
import mireka.imap.server.ProtocolLogger;
import mireka.imap.store.Mailbox;
import mireka.imap.store.MailboxName;
import mireka.imap.store.Repository;
import mireka.login.User;

/**
 * The Session class represents an IMAP session, it stores information collected during the session
 * and provides access to other classes related to the session.
 * 
 * Note that in IMAP RFC terms "Session" is a different thing, it is a sequence of client/server
 * interaction from the time that a mailbox is selected (SELECT or EXAMINE command) until the time
 * that selection ends. However when RFC 9051 mentions "IMAP session" in contrast to just "session",
 * than it seems to mean the full length of an IMAP connection.
 * 
 * Accessing this class is thread safe, because although multiple commands within the same session
 * can run concurrently in different threads, no two commands can run concurrently which depends on
 * each other regarding session state. The visibility of field changes is guaranteed by the locking
 * {@link ConcurrentCommands#registerInProgress} and {@link ConcurrentCommands#unregisterInProgress}
 * functions.
 */
public class Session {
    public final ImapServer server;
    public final Repository repository;
    public final SettingsRepo settingsRepo;
    public final AccessControl accessControl;
    public final Connection connection;
    public final ProtocolLogger protocolLogger;
    public final ConcurrentConnections connections;
    public final ConcurrentCommands commands;
    public SessionState state = SessionState.NOT_AUTHENTICATED;
    /**
     * The authenticated user. It may be null, if the user is not authenticated yet.
     */
    public User user;
    /**
     * The settings of the authenticated user. It may be null, if the user is not authenticated yet.
     */
    public Settings settings;
    /**
     * The selected mailbox, it may be null if no mailbox is selected.
     */
    public Mailbox mailbox;
    /**
     * The selected mailbox name, it may be null if no mailbox is selected.
     */
    public MailboxName mbname;
    /**
     * if the currently selected mailbox was opened as readonly using the EXAMINE command. Even user
     * specific flags can not be modified.
     */
    public boolean readonly;
    /**
     * true if the IMAP4rev2 capability is enabled
     */
    private boolean isCapIMAP4rev2;
    /**
     * True if UTF-8=ACCEPT capability is enabled.
     */
    private boolean isCapUTF8Accept;

    public Session(ImapServer server, Connection connection) {
        this.server = server;
        this.repository = server.getRepository();
        this.settingsRepo = server.settingsRepo;
        this.accessControl = server.accessControl;
        this.connection = connection;
        this.protocolLogger = connection.protocolLogger;
        this.connections = connection.serverThread.connections;
        this.commands = new ConcurrentCommands(connection);
    }

    public ResponseCode capabilitiesResponseCode() {
        return new ResponseCode() {

            @Override
            public void generate(Generator out) throws IOException {
                out.writeResponseCodeCapability(capabilities());
            }
        };
    }

    public List<String> capabilities() {
        List<String> l = new ArrayList<>();
        l.add("IMAP4rev2");

        // This is part of IMAP4rev2, it might be useful for older clients
        l.add("NAMESPACE");
        // This is part of IMAP4rev2, it might be useful for older clients
        l.add("UNSELECT");
        // This is part of IMAP4rev2, it might be useful for older clients
        l.add("UIDPLUS");
        // This is part of IMAP4rev2, it might be useful for older clients
        l.add("ENABLE");
        // This is part of IMAP4rev2, it might be useful for older clients
        l.add("IDLE");
        // This is part of IMAP4rev2, it might be useful for older clients
        l.add("SASL-IR");
        // This is mostly folded into IMAP4rev2, except multiple patterns in the LIST command, it
        // might be useful for older clients
        l.add("LIST-EXTENDED");
        // This is part of IMAP4rev2, it might be useful for older clients
        l.add("LIST-STATUS");

        // It seems that IMAP4rev2 mostly folded in this extension, but it is not explicitly listed.
        // See rfc6855. APPEND command syntax extension is not folded, however it is recommended to
        // implement this RFC if IMAP4rev1 compatibility is desired. It is not implemented now, and
        // it does cause error with Thunderbird, so this capability is disabled for now. It may be
        // necessary to implement this for effective search for IMAP4rev1 compatible clients
        // (Thunderbird as of 2022-04).
        // l.add("UTF8=ONLY");
        if (state == SessionState.NOT_AUTHENTICATED) {
            l.add("AUTH=PLAIN");
        }
        return l;
    }

    /**
     * Returns true, if the capability is actually enabled. Returns false if it was already enabled.
     */
    public boolean enableCapability(String cap) {
        if (cap.equals("IMAP4rev2") && !isCapIMAP4rev2) {
            isCapIMAP4rev2 = true;
            return true;
        } else if (cap.equals("UTF8=ACCEPT") && !isCapUTF8Accept) {
            isCapUTF8Accept = true;
            return true;
        }
        return false;
    }

    public void setAuthenticatedState(User user) throws IOException {
        connection.socket.setSoTimeout(Connection.POST_AUTHENTICATION_TIMEOUT);
        this.user = user;
        settings = settingsRepo.get(user);
        state = SessionState.AUTHENTICATED;
    }

    /**
     * Goes to AUTHENTICATED state from SELECTED state.
     */
    public void resetAuthenticatedState() {
        mailbox.removeSession(this);
        mailbox = null;
        protocolLogger.mailbox = null;
        mbname = null;
        readonly = false;
        state = SessionState.AUTHENTICATED;
    }

    /**
     * Selects the current mailbox and registers this session with the mailbox.
     * 
     * @throws NonExistentException if the mailbox is being deleted
     */
    public void setSelectedState(MailboxName mbname, Mailbox mailbox,
            boolean readonly) throws NonExistentException {
        if (this.mailbox == mailbox)
            return;
        if (state == SELECTED) {
            resetAuthenticatedState();
        }

        mailbox.addSession(this);
        this.mailbox = mailbox;
        protocolLogger.mailbox = mbname.storeName.name;
        this.mbname = mbname;
        this.readonly = readonly;
        state = SessionState.SELECTED;
    }

    public void setLogoutState() {
        state = SessionState.LOGOUT;
    }
}
