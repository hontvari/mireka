package mireka.startup;

import java.util.ArrayList;
import java.util.List;

/**
 * InjectableObjectContainer maintains a collection of objects which were
 * provided as possible default values for unassigned, injectable properties of
 * configuration objects. It can retrieve the default object which suits the
 * requested type.
 */
public class InjectableObjectContainer {
    private List<Object> objects = new ArrayList<>();

    /**
     * Registers the supplied object as a default object.
     * 
     * @param defaultObject
     *            The object to be registered
     */
    public void add(Object defaultObject) {
        objects.add(defaultObject);
    }

    /**
     * Returns the default object which is suitable for the specified type.
     * 
     * @param type
     *            The type for which a default object is requested.
     * @return The single suitable object which was found.
     * @throws IllegalArgumentException
     *             if zero or more than one object has been registered which is
     *             assignable to the specified type.
     */
    public Object get(Class<?> type) throws IllegalArgumentException {
        List<Object> found = new ArrayList<>();
        for (Object object : objects) {
            if (type.isAssignableFrom(object.getClass()))
                found.add(object);
        }
        if (found.size() > 1)
            throw new IllegalArgumentException(
                    "More than one default object meets the type " + type
                            + ": " + found);
        if (found.isEmpty())
            throw new IllegalArgumentException(
                    "There is no default object for the type " + type);
        return found.get(0);
    }
}
