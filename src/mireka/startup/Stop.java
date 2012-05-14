package mireka.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stop {
    private static final Logger logger = LoggerFactory.getLogger(Stop.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.exit(0);
    }

    public static void shutdown() {
        logger.info("Shutting down...");
        Lifecycle.callPreDestroyMethods();
        logger.info("Shutdown completed.");
    }

}
