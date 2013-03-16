package mireka.startup;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngineManager;

import mireka.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start provides functions which configure and start up Mireka.
 */
public class Start {
    private static final Logger logger = LoggerFactory.getLogger(Start.class);

    /**
     * Runs the configuration scripts and calls the {@link PostConstruct}
     * methods of the configured objects. It also registers a shutdown hook,
     * which will be called by the JRE on system exit, and in turn the shutdown
     * hook will call {@link Stop#shutdown()}.
     * 
     * @param args
     *            unused
     */
    public static void main(String[] args) {
        logger.info("Starting Mireka " + Version.getVersion() + "...");

        configure();

        addShutdownHook();

        startManagedObjects();

        logger.info("Startup completed.");
    }

    private static void configure() {
        try {
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptApi.engine = factory.getEngineByName("JavaScript");
            ScriptApi.engine.put("configuration", new ScriptApi());
            ScriptApi.include("conf/mireka.js");
        } catch (Exception e) {
            logger.error("Cannot read configuration. Include stack: "
                    + ScriptApi.includeStack.toString(), e);
            System.exit(78);
        }
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                Stop.shutdown();
            }

        });
    }

    private static void startManagedObjects() {
        try {
            Lifecycle.callPostConstructMethods();
        } catch (InvocationTargetException e) {
            logger.error(
                    "Startup failed because the @PostConstruct method of a "
                            + "startup object could not complete. Shutting down...",
                    e);
            System.exit(3);
        } catch (InvalidMethodSignatureException e) {
            logger.error("Startup failed. Shutting down...", e);
            System.exit(3);
        }
    }
}