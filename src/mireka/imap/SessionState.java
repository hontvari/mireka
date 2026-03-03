package mireka.imap;

/**
 * SessionState contains constants corresponding to the IMAP session states as defined by RFC 9051.
 */
public enum SessionState {
    NOT_AUTHENTICATED, AUTHENTICATED, SELECTED, LOGOUT;
}