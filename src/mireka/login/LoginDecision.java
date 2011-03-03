package mireka.login;

/**
 * This class represents the result of an authentication attempt.
 */
public enum LoginDecision {
    USERNAME_NOT_EXISTS, PASSWORD_DOES_NOT_MATCH, INVALID, VALID;
}
