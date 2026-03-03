package mireka.login;

/**
 * This class represents the result of an authentication attempt, it gives both
 * the decision, and if the attempt is successful, the {@link User}
 * authenticated.
 */
public class LoginResult {
    public final LoginDecision decision;
    /**
     * Represents the canonical name of the logged in user
     */
    public final User principal;

    public LoginResult(LoginDecision decision, User principal) {
        this.decision = decision;
        this.principal = principal;
    }
}
