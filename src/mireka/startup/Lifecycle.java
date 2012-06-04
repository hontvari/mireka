package mireka.startup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects objects which were created during the configuration phase and have a
 * lifecycle annotation, and provide functions to execute these lifecycle
 * methods later. Lifecycle annotations recognized are {@link PostConstruct} and
 * {@link PreDestroy}.
 */
public class Lifecycle {
    private static final Logger logger = LoggerFactory
            .getLogger(Lifecycle.class);
    public static final List<ManagedObject> managedObjects = new ArrayList<>();

    /**
     * Registers the object if it has at least one method which is marked with a
     * lifecycle annotation. This function must be called for every object which
     * were created during the configuration.
     * 
     * @param object
     *            the object which may have a lifecycle annotation
     */
    public static synchronized void addManagedObject(Object object) {
        if (object == null)
            throw new NullPointerException("Managed object must not be null");
        if (alreadyRegisteredStartup(object))
            throw new RuntimeException(
                    "Already registered for startup/shutdown: " + object);

        if (hasLifecycleAnnotation(object))
            managedObjects.add(new ManagedObject(object));
    }

    private static boolean alreadyRegisteredStartup(Object object) {
        for (ManagedObject managedObject : managedObjects) {
            if (managedObject.object == object)
                return true;
        }
        return false;
    }

    private static boolean hasLifecycleAnnotation(Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class))
                return true;
            if (method.isAnnotationPresent(PreDestroy.class))
                return true;
        }
        return false;
    }

    /**
     * Calls the methods of the registered objects which were marked with the
     * {@link PostConstruct} annotation in the order of their registration.
     * 
     * @throws InvocationTargetException
     *             thrown if a called method has thrown an exception
     * @throws InvalidMethodSignatureException
     *             thrown if the method could not be called because it has
     *             arguments.
     */
    public static synchronized void callPostConstructMethods()
            throws InvocationTargetException, InvalidMethodSignatureException {
        for (ManagedObject managedObject : managedObjects) {
            for (Method method : managedObject.object.getClass().getMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    try {
                        method.invoke(managedObject.object);
                        logger.debug("Object started: {}", managedObject.object);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidMethodSignatureException(
                                "@PostConstruct method must have an empty parameter list",
                                e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            managedObject.initialized = true;
        }
    }

    /**
     * Calls the methods of the successfully initialized registered objects
     * which were marked with the {@link PreDestroy} annotation in the opposite
     * order of their registrations. The PreDestroy method is not called on an
     * object
     * <ul>
     * <li>if it has a PostConstruct method which has thrown an exception during
     * startup
     * <li>another object which was registered earlier than this object has
     * thrown an exception in its PostConstruct method.
     * </ul>
     */
    public static synchronized void callPreDestroyMethods() {
        for (int i = managedObjects.size() - 1; i >= 0; i--) {
            ManagedObject managedObject = managedObjects.get(i);
            if (!managedObject.initialized)
                continue;
            for (Method method : managedObject.object.getClass().getMethods()) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    try {
                        method.invoke(managedObject.object);
                        logger.debug("Object stopped: {}", managedObject.object);
                    } catch (Exception e) {
                        logger.warn("PreDestroy function call failed on "
                                + managedObject.object, e);
                    }
                }
            }
        }
    }

    private static class ManagedObject {
        final Object object;
        @GuardedBy("Lifecycle.class")
        boolean initialized;

        public ManagedObject(Object object) {
            this.object = object;
        }
    }
}
