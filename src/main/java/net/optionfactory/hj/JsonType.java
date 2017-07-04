package net.optionfactory.hj;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

public class JsonType implements UserType, DynamicParameterizedType {

    public static final String TYPE = "net.optionfactory.hj.JsonType";

    public enum ColumnType {
        Text(Types.LONGVARCHAR), MysqlJson(Types.JAVA_OBJECT), PostgresJson(Types.JAVA_OBJECT), PostgresJsonb(Types.OTHER);

        private final int[] sqlType;

        ColumnType(int sqlType) {
            this.sqlType = new int[]{sqlType};
        }

        public int[] sqlTypes() {
            return this.sqlType;
        }
    }

    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Conf {

        String driver() default "";

        Class<? extends JsonDriverLocator> locator();

        ColumnType type() default ColumnType.Text;
    }

    private JsonDriver json;
    private TypeDescriptor type;
    private ColumnType ct;

    @Override
    public void setParameterValues(Properties properties) {
        final Class<?> declaringClass = UserTypes.entityClass(properties);
        final Field field = UserTypes.mappedField(declaringClass, properties);
        final JsonDriverLocator locator = UserTypes.makeLocator(field, properties);
        final Optional<String> driverName = UserTypes.driverName(field, properties);
        json = locator.locate(field.getAnnotations(), driverName);
        type = json.fieldType(field, declaringClass);
        ct = UserTypes.columnType(field, properties);
    }

    @Override
    public int[] sqlTypes() {
        return ct.sqlTypes();
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
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        final String string = rs.getString(names[0]);
        if (rs.wasNull() || string == null || string.isEmpty()) {
            return json.deserialize("null", type);
        }
        try {
            return json.deserialize(string, type);
        } catch (Exception ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement ps, Object value, int index, SharedSessionContractImplementor si) throws HibernateException, SQLException {
        if (value == null) {
            ps.setNull(index, ct.sqlTypes()[0]);
            return;
        }
        ps.getConnection().getMetaData().getDatabaseProductName();
        try {
            final String serialized = json.serialize(value, type);
            switch (ct) {
                case Text:
                case MysqlJson: {
                    ps.setString(index, serialized);
                }
                break;
                case PostgresJson: {
                    final PGobject postgresObject = new PGobject();
                    postgresObject.setType("json");
                    postgresObject.setValue(serialized);
                    ps.setObject(index, postgresObject);
                }
                break;
                case PostgresJsonb: {
                    final PGobject postgresObject = new PGobject();
                    postgresObject.setType("jsonb");
                    postgresObject.setValue(serialized);
                    ps.setObject(index, postgresObject);
                }
                break;
            }
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
