package net.optionfactory.hj;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * 
 * @author rferranti
 */
public interface JsonDriver {

    Object deserialize(String value, TypeDescriptor type);
    Object deserialize(String value, Type type);

    String serialize(Object value, TypeDescriptor type);
    String serialize(Object value, Type type);
    
    TypeDescriptor fieldType(Field field, Class<?> context);
    
}
