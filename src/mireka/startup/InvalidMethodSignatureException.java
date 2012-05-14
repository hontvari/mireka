package mireka.startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Thrown to indicate that a method annotated with {@link PostConstruct} or
 * {@link PreDestroy} has a non-empty argument list.
 */
public class InvalidMethodSignatureException extends Exception {
    private static final long serialVersionUID = -194173231516525335L;

    public InvalidMethodSignatureException(String message, Throwable cause) {
        super(message, cause);
    }

}
