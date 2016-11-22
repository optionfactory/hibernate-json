package net.optionfactory.hj.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.Test;
import org.junit.Assert;

public class JacksonJsonDriverTest {

    public static class Bean {

        public Optional<Integer> value;
    }

    @Test
    public void asd() throws JsonProcessingException, NoSuchFieldException {

        Bean b = new Bean();
        b.value = Optional.empty();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        JacksonJsonDriver driver = new JacksonJsonDriver(mapper);
        Field field = Bean.class.getField("value");
        String got = driver.serialize(b.value, driver.fieldType(field, Bean.class));
        Assert.assertEquals("null", got);

    }

    
}