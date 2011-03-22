package mireka.pop;

import mireka.pop.store.Maildrop;

/**
 * The Session class represents a POP3 session, it stores information collected
 * during the session and provides access to other classes related to the
 * session.
 */
public class Session {
    private final PopServer server;
    private final SessionThread thread;
    private SessionState sessionState = SessionState.AUTHORIZATION;

    private Maildrop maildrop;
    private boolean tlsStarted;

    public Session(PopServer server, SessionThread sessionThread) {
        this.server = server;
        this.thread = sessionThread;
    }

    /**
     * @category GETSET
     */
    public PopServer getServer() {
        return server;
    }

    /**
     * @category GETSET
     */
    public SessionState getSessionState() {
        return sessionState;
    }

    /**
     * @category GETSET
     */
    public void setSessionState(SessionState sessionState) {
        this.sessionState = sessionState;
    }

    /**
     * @category GETSET
     */
    public Maildrop getMaildrop() {
        return maildrop;
    }

    /**
     * @category GETSET
     */
    public void setMaildrop(Maildrop maildrop) {
        this.maildrop = maildrop;
    }

    /**
     * @category GETSET
     */
    public SessionThread getThread() {
        return thread;
    }

    /**
     * @category GETSET
     */
    public boolean isTlsStarted() {
        return tlsStarted;
    }

    /**
     * @category GETSET
     */
    public void setTlsStarted(boolean started) {
        tlsStarted = started;
    }

}
