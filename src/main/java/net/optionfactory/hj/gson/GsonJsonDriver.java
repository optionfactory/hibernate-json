package net.optionfactory.hj.gson;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Field;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.JsonMappingException;
import net.optionfactory.hj.TypeDescriptor;

public class GsonJsonDriver implements JsonDriver {

    private final Gson json;

    public GsonJsonDriver(Gson json) {
        this.json = json;
    }
    
    
    @Override
    public Object deserialize(String value, TypeDescriptor type) {
        try{
            return json.fromJson(value, type.as(TypeToken.class).getType());
        }catch(JsonParseException ex){
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public String serialize(Object value, TypeDescriptor type) {
        try{
            return json.toJson(value, type.as(TypeToken.class).getType());
        }catch(JsonParseException ex){
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public TypeDescriptor fieldType(Field field, Class<?> context) {
        return new GsonTypeDescriptor(TypeToken.get(field.getGenericType()));
    }

}
