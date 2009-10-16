package mireka.submission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;

/**
 * TODO: prevent brute-force attack on passwords
 */
public class UsernamePasswordValidatorImpl implements UsernamePasswordValidator {
    private final Logger logger =
            LoggerFactory.getLogger(UsernamePasswordValidatorImpl.class);
    private LoginSpecification loginSpecification;

    @Override
    public void login(String username, String password)
            throws LoginFailedException {
        LoginResult loginResult =
                loginSpecification.evaluate(username, password);
        switch (loginResult) {
        case VALID:
            logger.debug("{} logged in", username);
            return;
        case PASSWORD_DOES_NOT_MATCH:
            logger.debug("Password doesn't match for username {}", username);
            throw new LoginFailedException(
                    "Password doesn't match for username " + username);
        case USERNAME_NOT_EXISTS:
            logger.debug("Username {} doesn't exist", username);
            throw new LoginFailedException("Username " + username
                    + " doesn't exist");
        }
    }

    /**
     * @category GETSET
     */
    public LoginSpecification getLoginSpecification() {
        return loginSpecification;
    }

    /**
     * @category GETSET
     */
    public void setLoginSpecification(LoginSpecification users) {
        this.loginSpecification = users;
    }

}
