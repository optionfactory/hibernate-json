package net.optionfactory.hj.jackson;

import com.fasterxml.jackson.databind.JavaType;
import net.optionfactory.hj.TypeDescriptor;

/**
 * 
 * @author rferranti
 */
public class JacksonTypeDescriptor implements TypeDescriptor {

    private final JavaType javaType;

    public JacksonTypeDescriptor(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public <T> T as(Class<T> cls) {
        if(cls != JavaType.class){
            throw new IllegalArgumentException("JacksonTypeDescriptor can only be cast to JavaType");
        }
        return (T) javaType;
    }

    @Override
    public Class<?> rawClass() {
        return javaType.getRawClass();
    }
}
