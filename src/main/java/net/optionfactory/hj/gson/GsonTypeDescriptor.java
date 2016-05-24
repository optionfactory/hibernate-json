package net.optionfactory.hj.gson;

import com.google.gson.reflect.TypeToken;
import net.optionfactory.hj.TypeDescriptor;

/**
 * 
 * @author rferranti
 */
public class GsonTypeDescriptor implements TypeDescriptor {

    private final TypeToken type;

    public GsonTypeDescriptor(TypeToken type) {
        this.type = type;
    }
    
    
    @Override
    public <T> T as(Class<T> cls) {
        if(cls != TypeToken.class){
            throw new IllegalArgumentException("GsonTypeDescriptor can only be cast to TypeToken");
        }
        return (T) type;
    }

    @Override
    public Class<?> rawClass() {
        return type.getRawType();
    }

}
