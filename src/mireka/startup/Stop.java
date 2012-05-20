package mireka.startup;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stop contains methods for stopping Mireka.
 */
public class Stop {
    private static final Logger logger = LoggerFactory.getLogger(Stop.class);

    /**
     * Exits the Mireka process. Some service wrappers require a main class
     * which they can call on a service shutdown request.
     * 
     * @param args
     *            not used
     */
    public static void main(String[] args) {
        System.exit(0);
    }

    /**
     * Calls the {@link PreDestroy} method of the configured objects.
     */
    public static void shutdown() {
        logger.info("Shutting down...");
        Lifecycle.callPreDestroyMethods();
        logger.info("Shutdown completed.");
    }

}
