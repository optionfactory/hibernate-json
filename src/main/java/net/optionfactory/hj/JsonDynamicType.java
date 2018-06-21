package net.optionfactory.hj;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.Properties;
import net.optionfactory.hj.JsonType.ColumnType;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

public class JsonDynamicType implements UserType, DynamicParameterizedType {

    public static final String TYPE = "net.optionfactory.hj.JsonDynamicType";

    private JsonDriver json;
    private TypeDescriptor staticType;
    private ColumnType ct;

    @Override
    public void setParameterValues(Properties properties) {
        final Class<?> declaringClass = UserTypes.entityClass(properties);
        final Field field = UserTypes.mappedField(declaringClass, properties);
        final JsonDriverLocator locator = UserTypes.makeLocator(field, properties);
        final Optional<String> driverName = UserTypes.driverName(field, properties);
        json = locator.locate(field.getAnnotations(), driverName);
        staticType = json.fieldType(field, declaringClass);
        ct = UserTypes.columnType(field, properties);
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{
            Types.LONGVARCHAR,
            ct.sqlTypes()[0]
        };
    }

    @Override
    public Class<?> returnedClass() {
        return staticType.rawClass();
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
        final String savedTypeString = rs.getString(names[0]);
        if (savedTypeString == null) {
            return null;
        }
        try {
            final Class<?> savedType = Class.forName(savedTypeString);
            final String savedValue = rs.getString(names[1]);
            if (rs.wasNull() || savedValue == null || savedValue.isEmpty()) {
                return json.deserialize("null", savedType);
            }
            try {
                return json.deserialize(savedValue, savedType);
            } catch (Exception ex) {
                throw new JsonMappingException(ex);
            }
        } catch (ClassNotFoundException ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement ps, Object value, int index, SharedSessionContractImplementor si) throws HibernateException, SQLException {
        if (value == null) {
            ps.setNull(index, Types.LONGVARCHAR);
            ps.setNull(index + 1, ct.sqlTypes()[0]);
            return;
        }
        ps.getConnection().getMetaData().getDatabaseProductName();
        try {
            final String serialized = json.serialize(value, value.getClass());
            ps.setString(index, value.getClass().getName());
            switch (ct) {
                case Text:
                case MysqlJson: {
                    ps.setString(index + 1, serialized);
                }
                break;
                case PostgresJson: {
                    final PGobject postgresObject = new PGobject();
                    postgresObject.setType("json");
                    postgresObject.setValue(serialized);
                    ps.setObject(index + 1, postgresObject);
                }
                break;
                case PostgresJsonb: {
                    final PGobject postgresObject = new PGobject();
                    postgresObject.setType("jsonb");
                    postgresObject.setValue(serialized);
                    ps.setObject(index + 1, postgresObject);
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
        if (value == null) {
            return null;
        }
        try {
            final CacheableForm cacheableForm = new CacheableForm();
            cacheableForm.type = value.getClass();
            cacheableForm.json = json.serialize(value, value.getClass());
            return cacheableForm;
        } catch (Exception ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        if (cached instanceof CacheableForm == false) {
            throw new HibernateException(String.format("Cached object is not a CacheableForm: '%s'", cached));
        }
        try {
            final CacheableForm cf = (CacheableForm) cached;
            return json.deserialize(cf.json, cf.type);
        } catch (Exception ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    private static class CacheableForm implements Serializable {

        public Class<?> type;
        public String json;

    }

}
