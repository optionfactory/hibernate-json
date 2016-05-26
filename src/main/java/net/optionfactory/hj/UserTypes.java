package net.optionfactory.hj;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Properties;
import net.optionfactory.hj.JsonType.ColumnType;
import net.optionfactory.hj.spring.SpringDriverLocator;
import org.hibernate.usertype.DynamicParameterizedType;

public class UserTypes {

    public static final String DRIVER_NAME_KEY = "jsonDriverName";
    public static final String DRIVER_LOCATOR_CLASS_KEY = "jsonDriverLocatorClass";
    public static final String COLUMN_TYPE_KEY = "columnType";

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
        final String driverName = searchAnnotation(field, JsonType.Conf.class)
                .map(a -> a.driver())
                .orElseGet(() -> properties.getProperty(DRIVER_NAME_KEY, ""));
        return driverName.isEmpty() ? Optional.empty() : Optional.of(driverName);
    }

    public static JsonDriverLocator makeLocator(final Field field, Properties properties) {
        try {
            final Class locatorClass = searchAnnotation(field, JsonType.Conf.class)
                    .map(a -> a.locator())
                    .orElseGet(() -> {
                        try{
                            return (Class) Class.forName(properties.getProperty(DRIVER_LOCATOR_CLASS_KEY, SpringDriverLocator.class.getName()));
                        }catch(ClassNotFoundException ex){
                            throw new IllegalStateException(String.format("DriverLocator class not found: %s", properties.getProperty(DRIVER_LOCATOR_CLASS_KEY)));
                        }
                    });
            return (JsonDriverLocator) locatorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("Cannot instantiate DriverLocator", ex);
        }
    }
    
    

    public static ColumnType columnType(Field field, Properties properties) {
        return searchAnnotation(field, JsonType.Conf.class)
                .map(a -> a.type())
                .orElseGet(() -> ColumnType.valueOf(properties.getProperty(COLUMN_TYPE_KEY, "Text")));
    }
    
    
    private static <T extends Annotation> Optional<T> searchAnnotation(Field field, Class<T> annotationClass){
        final T annotation = field.getAnnotation(annotationClass);
        if(annotation != null){
            return Optional.of(annotation);
        }
        for (Annotation fieldAnnotation : field.getAnnotations()) {
            Optional<T> up = searchAnnotation(fieldAnnotation, annotationClass);
            if(up.isPresent()){
                return up;
            }            
        }
        return Optional.empty();
    }
    
    public static <T extends Annotation> Optional<T> searchAnnotation(Annotation annotation, Class<T> annotationClass){
        for (Annotation annotationOnAnnotation : annotation.annotationType().getAnnotations()) {
            if(annotationOnAnnotation.annotationType() == annotationClass){
                return Optional.of((T)annotationOnAnnotation);
            }
        }
        return Optional.empty();
    }    

}
