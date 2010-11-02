package mireka;

/**
 * Thrown to indicate that the configuration is invalid. Some configuration
 * problems cannot be detected at startup, this unchecked exception signals such
 * a condition. An SMTP service can reply with a more specific error message by
 * catching this exception.
 */
public class ConfigurationException extends RuntimeException {
    private static final long serialVersionUID = -6903587685288695817L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
