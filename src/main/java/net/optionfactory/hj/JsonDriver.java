package net.optionfactory.hj;

import java.lang.reflect.Field;

/**
 * 
 * @author rferranti
 */
public interface JsonDriver {

    Object deserialize(String value, TypeDescriptor type);

    String serialize(Object value, TypeDescriptor type);
    
    TypeDescriptor fieldType(Field field, Class<?> context);
    
}
