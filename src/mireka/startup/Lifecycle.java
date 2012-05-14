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

public class Lifecycle extends Start {
    private static final Logger logger = LoggerFactory
            .getLogger(Lifecycle.class);
    public static final List<ManagedObject> managedObjects = new ArrayList<>();

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
                    managedObject.initialized = true;
                }
            }
        }
    }

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
