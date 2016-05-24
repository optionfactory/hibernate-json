package net.optionfactory.hj;

import net.optionfactory.hj.spring.SpringDriverLocator;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

public class JsonType implements UserType, DynamicParameterizedType {

    public static final String TYPE = "net.optionfactory.hj.JsonType";
    private static final String WITH_DRIVER_PROPERTY_NAME = "net.optionfactory.hj.driver";
    private static final String WITH_LOCATOR_PROPERTY_NAME = "net.optionfactory.hj.locator";
    private static final int[] SQL_TYPES = new int[]{
        StandardBasicTypes.TEXT.sqlType()
    };

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface WithDriver {

        String value() default "";

        Class<? extends DriverLocator> locator() default SpringDriverLocator.class;
    }

    private JsonDriver json;
    private TypeDescriptor type;

    public interface DriverLocator {
        JsonDriver locate(Optional<String> name);
    }


    @Override
    public void setParameterValues(Properties properties) {
        try {
            final Class<?> declaringClass = Class.forName(properties.getProperty(DynamicParameterizedType.ENTITY));
            final Field field = mappedField(declaringClass, properties.getProperty(DynamicParameterizedType.PROPERTY));
            final String driverName = field.getAnnotation(WithDriver.class) != null 
                    ? field.getAnnotation(WithDriver.class).value() 
                    : properties.getProperty(WITH_DRIVER_PROPERTY_NAME, "");
            final Class locatorClass = field.getAnnotation(WithDriver.class) != null
                    ? field.getAnnotation(WithDriver.class).locator()
                    : Class.forName(properties.getProperty(WITH_LOCATOR_PROPERTY_NAME, SpringDriverLocator.class.getName()));
            final DriverLocator locator = (DriverLocator)locatorClass.newInstance();
            json = locator.locate(driverName.isEmpty() ? Optional.empty() : Optional.of(driverName));
            type = json.fieldType(field, declaringClass);
        } catch (InstantiationException | IllegalAccessException ex ) {
            throw new IllegalStateException("Cannot instantiate DriverLocator", ex);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(String.format("Entity class not found: %s", properties.getProperty(DynamicParameterizedType.ENTITY)));
        }
    }

    private static Field mappedField(Class<?> baseClass, String fieldName) {
        for (Class<?> cls = baseClass; cls != null; cls = cls.getSuperclass()) {
            for (Field f : cls.getDeclaredFields()) {
                if (fieldName.equals(f.getName())) {
                    return f;
                }
            }
        }
        throw new IllegalStateException(String.format("Entity field not found: %s::%s", baseClass, fieldName));
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class returnedClass() {
        return type.rawClass();
    }

    @Override
    public boolean equals(Object lhs, Object rhs) throws HibernateException {
        return lhs == null ? rhs == null : lhs.equals(rhs);
    }

    @Override
    public int hashCode(Object obj) throws HibernateException {
        return obj == null ? 0 : obj.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor si, Object owner) throws HibernateException, SQLException {
        String string = rs.getString(names[0]);
        if (rs.wasNull() || string == null || string.isEmpty()) {
            return null;
        }
        try {
            return json.deserialize(string, type);
        } catch (Exception ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement ps, Object value, int index, SessionImplementor si) throws HibernateException, SQLException {
        if (value == null) {
            ps.setNull(index, SQL_TYPES[0]);
            return;
        }
        try {
            ps.setString(index, json.serialize(value, type));
        } catch (Exception ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        try {
            return json.serialize(value, type);
        } catch (Exception ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        if (cached instanceof String == false) {
            throw new HibernateException(String.format("Cached object is not a string: '%s'", cached));
        }
        try {
            return json.deserialize((String) cached, type);
        } catch (Exception ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

}
