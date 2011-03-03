package mireka.pop;

/**
 * SessionState contains constants corresponding to the POP3 session states as
 * defined by RFC 1939.
 */
public enum SessionState {
    AUTHORIZATION, AUTHORIZATION_PASS_COMMAND_EXPECTED, TRANSACTION, UPDATE;
}