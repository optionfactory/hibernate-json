package net.optionfactory.hj.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import org.junit.Test;
import org.junit.Assert;

public class JacksonJsonDriverTest {

    public static class Bean<T> {

        public T value;
    }
    
    public static class LongBean extends Bean<Number> {}

    @Test
    public void testRegisteredJacksonTypeModifiersAreInvoked() throws JsonProcessingException, NoSuchFieldException {

        Bean<Number> b = new LongBean();
        b.value = 3l;

        final SpyTypeModifier spyTypeModifier = new SpyTypeModifier();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(mapper.getTypeFactory().withModifier(spyTypeModifier));
        JacksonJsonDriver driver = new JacksonJsonDriver(mapper);
        Field field = Bean.class.getField("value");
        driver.serialize(b.value, driver.fieldType(field, LongBean.class));
        Assert.assertTrue("Configured type modifier must be called", spyTypeModifier.called);

    }

    public static class SpyTypeModifier extends TypeModifier {

        public boolean called = false;

        @Override
        public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
            called = true;
            return type;
        }
    }

}
