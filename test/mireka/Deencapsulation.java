package mireka;

import java.lang.reflect.Field;
import java.util.Objects;

public class Deencapsulation {

    public static Object getField(Object instance, String field) {
        try {
            Objects.requireNonNull(instance);
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getField(Object instance, String field, Class<T> type) {
        return type.cast(getField(instance, field));
    }

    public static void setField(Object instance, String field, Object value) {
        try {
            Objects.requireNonNull(instance);
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
