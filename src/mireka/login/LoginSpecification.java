package mireka.login;

/**
 * Verifies login credentials like username, password combinations, returns
 * detailed result
 */
public interface LoginSpecification {

    public abstract LoginResult evaluatePlain(String username, String password);

    public abstract LoginResult evaluateApop(String username, String timestamp,
            byte[] digestBytes);

}