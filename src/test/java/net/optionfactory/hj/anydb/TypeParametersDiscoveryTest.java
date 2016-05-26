package net.optionfactory.hj.anydb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.TypeDescriptor;
import net.optionfactory.hj.gson.GsonJsonDriver;
import net.optionfactory.hj.jackson.JacksonJsonDriver;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author rferranti
 */
public class TypeParametersDiscoveryTest {
    
    private final JsonDriver jacksonDriver = new JacksonJsonDriver(new ObjectMapper());
    private final JsonDriver gsonDriver = new GsonJsonDriver(new Gson());

    
    public static class SimpleBean {

        public Long data;

    }

    @Test
    public void canMapSimpleBeanWithJacksonDriver() throws JsonProcessingException, NoSuchFieldException {
        final SimpleBean b = new SimpleBean();
        b.data = 2L;
        final TypeDescriptor fieldType = jacksonDriver.fieldType(SimpleBean.class.getDeclaredField("data"), SimpleBean.class);
        final String got = jacksonDriver.serialize(b.data, fieldType);
        final Object deserialized = jacksonDriver.deserialize(got, fieldType);
        Assert.assertEquals(Long.class, deserialized.getClass());
    }

    @Test
    public void canMapSimpleBeanWithGsonDriver() throws JsonProcessingException, NoSuchFieldException {
        final SimpleBean b = new SimpleBean();
        b.data = 2L;
        final TypeDescriptor fieldType = gsonDriver.fieldType(SimpleBean.class.getDeclaredField("data"), SimpleBean.class);
        final String got = gsonDriver.serialize(b.data, fieldType);
        final Object deserialized = gsonDriver.deserialize(got, fieldType);
        Assert.assertEquals(Long.class, deserialized.getClass());
    }    
    
    public static class GenericBean<T> {

        public Map<String, T> data;

    }
    
    public static class ReifiedBean extends GenericBean<Long> {
    
    }

    @Test
    public void typeParametersAreCorrectlyDiscoveredWithJacksonDriverForGenericBeans() throws JsonProcessingException, NoSuchFieldException {
        final JsonDriver driver = new JacksonJsonDriver(new ObjectMapper());
        final ReifiedBean b = new ReifiedBean();
        b.data = Collections.singletonMap("key", 2L);
        final TypeDescriptor fieldType = driver.fieldType(GenericBean.class.getDeclaredField("data"), ReifiedBean.class);
        final String got = driver.serialize(b.data, fieldType);
        final Map deserialized = (Map)driver.deserialize(got, fieldType);
        Assert.assertEquals(Long.class, deserialized.get("key").getClass());
    }

    @Test
    @Ignore
    public void typeParametersAreCorrectlyDiscoveredWithGsonDriverForGenericBeans() throws JsonProcessingException, NoSuchFieldException {
        final JsonDriver driver = new GsonJsonDriver(new Gson());
        final ReifiedBean b = new ReifiedBean();
        b.data = Collections.singletonMap("key", 2L);
        
        final TypeDescriptor fieldType = driver.fieldType(GenericBean.class.getDeclaredField("data"), ReifiedBean.class);
        final String got = driver.serialize(b.data, fieldType);
        final Map deserialized = (Map)driver.deserialize(got, fieldType);
        Assert.assertEquals(Long.class, deserialized.get("key").getClass());
    }

    
    public static class ReallyNestedGenericBean<T> {

        public Map<String, List<List<T>>> data;

    }
    
    public static class ReifiedReallyNestedBean extends ReallyNestedGenericBean<Long> {
    
    }
    
    @Test
    public void typeParametersAreCorrectlyDiscoveredWithJacksonDriverForReallyNestedGenericBeans() throws JsonProcessingException, NoSuchFieldException {
        final JsonDriver driver = new JacksonJsonDriver(new ObjectMapper());
        final ReifiedReallyNestedBean b = new ReifiedReallyNestedBean();
        b.data = Collections.singletonMap("key", Arrays.asList(Arrays.asList(2L)));
        final TypeDescriptor fieldType = driver.fieldType(ReallyNestedGenericBean.class.getDeclaredField("data"), ReifiedReallyNestedBean.class);
        final String got = driver.serialize(b.data, fieldType);
        final Map<String, List<List<?>>> deserialized = (Map)driver.deserialize(got, fieldType);
        Assert.assertEquals(Long.class, deserialized.get("key").get(0).get(0).getClass());
    }
    
    @Test
    @Ignore
    public void typeParametersAreCorrectlyDiscoveredWithGsonDriverForReallyNestedGenericBeans() throws JsonProcessingException, NoSuchFieldException {
        final JsonDriver driver = new GsonJsonDriver(new Gson());
        final ReifiedReallyNestedBean b = new ReifiedReallyNestedBean();
        b.data = Collections.singletonMap("key", Arrays.asList(Arrays.asList(2L)));
        final TypeDescriptor fieldType = driver.fieldType(ReallyNestedGenericBean.class.getDeclaredField("data"), ReifiedReallyNestedBean.class);
        final String got = driver.serialize(b.data, fieldType);
        final Map<String, List<List<?>>> deserialized = (Map)driver.deserialize(got, fieldType);
        Assert.assertEquals(Long.class, deserialized.get("key").get(0).get(0).getClass());
    }

        
    
}
