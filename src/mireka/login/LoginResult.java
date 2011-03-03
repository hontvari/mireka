package mireka.login;

/**
 * This class represents the result of an authentication attempt, it gives both
 * the decision, and if the attempt is successful, the {@link Principal}
 * authenticated.
 */
public class LoginResult {
    public final LoginDecision decision;
    /**
     * Represents the canonical name of the logged in user
     */
    public final Principal principal;

    public LoginResult(LoginDecision decision, Principal principal) {
        this.decision = decision;
        this.principal = principal;
    }
}
