package mireka.startup;

import java.lang.reflect.InvocationTargetException;

import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {
    private static final Logger logger = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {
        configure();

        addShutdownHook();

        startManagedObjects();
    }

    private static void configure() {
        try {
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptApi.engine = factory.getEngineByName("JavaScript");
            ScriptApi.engine.put("mireka", new ScriptApi());
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
                    e.getCause());
            System.exit(3);
        } catch (InvalidMethodSignatureException e) {
            logger.error("Startup failed. Shutting down...", e);
            System.exit(3);
        }
    }
}