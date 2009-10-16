package mireka.submission;

/**
 * verifies username, password combinations, returns detailed result
 */
public interface LoginSpecification {

    public abstract LoginResult evaluate(String username, String password);

}