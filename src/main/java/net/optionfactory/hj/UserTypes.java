package net.optionfactory.hj;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Properties;
import net.optionfactory.hj.spring.SpringDriverLocator;
import org.hibernate.usertype.DynamicParameterizedType;

public class UserTypes {

    public static Class<?> entityClass(Properties properties) {
        try {
            return Class.forName(properties.getProperty(DynamicParameterizedType.ENTITY));
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(String.format("Entity class not found: %s", properties.getProperty(DynamicParameterizedType.ENTITY)));
        }
    }

    public static Field mappedField(Class<?> baseClass, Properties properties) {
        final String fieldName = properties.getProperty(DynamicParameterizedType.PROPERTY);
        for (Class<?> cls = baseClass; cls != null; cls = cls.getSuperclass()) {
            for (Field f : cls.getDeclaredFields()) {
                if (fieldName.equals(f.getName())) {
                    return f;
                }
            }
        }
        throw new IllegalStateException(String.format("Entity field not found: %s::%s", baseClass, fieldName));
    }

    public static Optional<String> driverName(final Field field, Properties properties) {
        final String driverName = field.getAnnotation(JsonType.WithDriver.class) != null ? field.getAnnotation(JsonType.WithDriver.class).value() : properties.getProperty(JsonType.WITH_DRIVER_PROPERTY_NAME, "");
        return driverName.isEmpty() ? Optional.empty() : Optional.of(driverName);
    }

    public static JsonDriverLocator makeLocator(final Field field, Properties properties) {
        try {
            final Class locatorClass = field.getAnnotation(JsonType.WithDriver.class) != null ? field.getAnnotation(JsonType.WithDriver.class).locator() : Class.forName(properties.getProperty(JsonType.WITH_LOCATOR_PROPERTY_NAME, SpringDriverLocator.class.getName()));
            return (JsonDriverLocator) locatorClass.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(String.format("DriverLocator class not found: %s", properties.getProperty(JsonType.WITH_LOCATOR_PROPERTY_NAME)));
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("Cannot instantiate DriverLocator", ex);
        }
    }

}
