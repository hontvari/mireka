package mireka.startup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DependencyInjection maintains a collection of injectable default objects, and
 * is able to inject those objects into uninitialized properties of other
 * configuration objects.
 */
public class DependencyInjection {
    private static final Logger logger = LoggerFactory
            .getLogger(DependencyInjection.class);
    private static InjectableObjectContainer injectableObjects =
            new InjectableObjectContainer();

    /**
     * Registers the object as an object which can be injected later into
     * uninitialized properties.
     * 
     * @param object
     *            The object which can be injected
     */
    public static void addInjectable(Object object) {
        logger.debug("Injectable object registered: " + object);
        injectableObjects.add(object);
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
     */
    public static void injectDependencies(Object object,
            List<String> initializedProperties) {
        for (Method method : object.getClass().getMethods()) {
            if (!method.isAnnotationPresent(Inject.class))
                continue;
            if (isExplicitlyInitialized(method, initializedProperties))
                continue;
            injectIntoMethod(object, method);
        }
    }

    private static boolean isExplicitlyInitialized(Method method,
            List<String> initializedProperties) {
        String property = calculatePropertyName(method);
        return initializedProperties.contains(property);
    }

    private static String calculatePropertyName(Method method) {
        String methodName = method.getName();
        if (methodName.length() < 4 || !methodName.startsWith("set")
                || !Character.isUpperCase(methodName.charAt(3)))
            throw new RuntimeException(
                    "Method name does not conform to the JavaBean setter "
                            + "naming convention (setX): " + method.toString());
        return Character.toLowerCase(methodName.charAt(3))
                + methodName.substring(4);
    }

    private static void injectIntoMethod(Object object, Method method) {
        Class<?> type = method.getParameterTypes()[0];
        Object dependency = injectableObjects.get(type);
        try {
            method.invoke(object, dependency);
        } catch (IllegalAccessException e) {
            // impossible
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Dependency injection failed, because the setter has thrown "
                            + "an exception. Object: " + object + ". Method: "
                            + method + ".", e.getCause());
        }
        logger.debug("Dependency " + dependency
                + " has been injected into the "
                + calculatePropertyName(method) + " property of " + object);
    }
}
