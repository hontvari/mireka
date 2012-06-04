package mireka.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScriptApi provides functions which are called from the configuration scripts.
 */
public class ScriptApi {
    private static final Logger logger = LoggerFactory
            .getLogger(ScriptApi.class);
    public static ScriptEngine engine;
    public static final Stack<File> includeStack = new Stack<>();

    /**
     * Include another configuration file.
     * 
     * @return the result of the included file, in case of Javascript, this is
     *         the result of the last statement.
     * @param fileName
     *            the path to the script file to be included. Relative paths are
     *            resolved against the mireka.home directory.
     * @throws IOException
     *             thrown if the file cannot be opened
     * @throws ScriptException
     *             thrown if the script in the included file cannot be run
     *             because of a syntactic or semantic error
     */
    public static Object include(String fileName) throws IOException,
            ScriptException {
        File file = new File(fileName);
        String oldSourceFileName = (String) engine.get(ScriptEngine.FILENAME);
        engine.put(ScriptEngine.FILENAME, file.toString());
        includeStack.push(file);
        try (InputStreamReader reader =
                new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            logger.debug("Evaluating " + file.toString() + "...");
            Object result = engine.eval(reader);
            logger.debug("Completed " + file.toString());
            includeStack.pop();
            engine.put(ScriptEngine.FILENAME, oldSourceFileName);
            return result;
        }
    }

    /**
     * Registers objects created by the configuration script for lifecycle
     * management, which includes calling methods marked with
     * {@link PostConstruct} and {@link PreDestroy} annotations.
     * 
     * @param object
     *            the object which may have a lifecycle annotation.
     * @return the object argument
     * 
     * @see Lifecycle
     */
    public static Object manage(Object object) {
        Lifecycle.addManagedObject(object);
        return object;
    }

    /**
     * Registers the object as an object which can be injected later into an
     * uninitialized property.
     * 
     * @param object
     *            The object which can be injected
     * @return the object argument
     * @see DependencyInjection#addInjectable(Object)
     */
    public static Object addInjectableObject(Object object) {
        DependencyInjection.addInjectable(object);
        return object;
    }

    /**
     * Initializes the properties that has not got a value explicitly and are
     * annotated with @Inject annotation with default objects selected from the
     * set of injectable objects.
     * 
     * @param object
     *            The object that may have uninitialized properties
     * @param initializedProperties
     *            The list of properties which were explicitly initialized.
     * @see DependencyInjection#injectDependencies(Object, List)
     */
    public static void injectMissingPropertyValues(Object object,
            List<String> initializedProperties) {
        DependencyInjection.injectDependencies(object, initializedProperties);
    }

}
