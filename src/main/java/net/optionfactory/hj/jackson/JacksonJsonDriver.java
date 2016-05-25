package net.optionfactory.hj.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import net.optionfactory.hj.JsonMappingException;
import net.optionfactory.hj.TypeDescriptor;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.jackson.reflection.ResolvableType;

/**
 *
 * @author rferranti
 */
public class JacksonJsonDriver implements JsonDriver {

    private final ObjectMapper mapper;

    public JacksonJsonDriver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String serialize(Object value, TypeDescriptor type) {
        try {
            return writeValueAsString(value, type.as(JavaType.class));
        } catch (JsonProcessingException ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public Object deserialize(String value, TypeDescriptor type) {
        try {
            return mapper.readValue(value, type.as(JavaType.class));
        } catch (IOException ex) {
            throw new JsonMappingException(ex);
        }
    }

    @Override
    public JacksonTypeDescriptor fieldType(Field field, Class<?> context) {        
        final ResolvableType rt = ResolvableType.forField(field, context);
        return new JacksonTypeDescriptor(resolvableTypeToJavaType(rt));
    }
    
    
    
    private JavaType resolvableTypeToJavaType(ResolvableType source){
        final JavaType[] generics = Arrays.stream(source.getGenerics()).map(this::resolvableTypeToJavaType).toArray(n -> new JavaType[n]);
        return mapper.getTypeFactory().constructParametricType(source.resolve(), generics);
    }

    /**
     * Missing overload in ObjectMapper using a JavaType and enabling to specify generics context 
     * in which the value has to be serialized.
     */
    private String writeValueAsString(Object value, JavaType type) throws JsonProcessingException {
        // alas, we have to pull the recycler directly here...
        SegmentedStringWriter sw = new SegmentedStringWriter(mapper.getFactory()._getBufferRecycler());
        try {
            JsonGenerator g = mapper.getFactory().createGenerator(sw);
            SerializationConfig cfg = mapper.getSerializationConfig();
            cfg.initialize(g); // since 2.5
            if (cfg.isEnabled(SerializationFeature.CLOSE_CLOSEABLE) && (value instanceof Closeable)) {
                _configAndWriteCloseable(g, value, cfg);
                return sw.getAndClear();
            }
            boolean closed = false;
            try {
                _serializerProvider(cfg).serializeValue(g, value, type);
                closed = true;
                g.close();
                return sw.getAndClear();
            } finally {
                /* won't try to close twice; also, must catch exception (so it 
                 * will not mask exception that is pending)
                 */
                if (!closed) {
                    /* 04-Mar-2014, tatu: But! Let's try to prevent auto-closing of
                     * structures, which typically causes more damage.
                     */
                    g.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
                    try {
                        g.close();
                    } catch (IOException ioe) {
                    }
                }
            }

        } catch (JsonProcessingException e) { // to support [JACKSON-758]
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw com.fasterxml.jackson.databind.JsonMappingException.fromUnexpectedIOE(e);
        }
    }
    
    // duplicated from ObjectMapper for visibility reasons.
    private void _configAndWriteCloseable(JsonGenerator g, Object value, SerializationConfig cfg)
        throws IOException
    {
        Closeable toClose = (Closeable) value;
        try {
            _serializerProvider(cfg).serializeValue(g, value);
            JsonGenerator tmpGen = g;
            g = null;
            tmpGen.close();
            Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        } finally {
            // Need to close both generator and value, as long as they haven't yet been closed
            if (g != null) {
                // 04-Mar-2014, tatu: But! Let's try to prevent auto-closing of
                //    structures, which typically causes more damage.
                g.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
                try {
                    g.close();
                } catch (IOException ioe) { }
            }
            if (toClose != null) {
                try {
                    toClose.close();
                } catch (IOException ioe) { }
            }
        }
    }    
    
    // duplicated from ObjectMapper for visibility reasons.
    private DefaultSerializerProvider _serializerProvider(SerializationConfig cfg) {
        return ((DefaultSerializerProvider) mapper.getSerializerProvider()).createInstance(cfg, mapper.getSerializerFactory());
    }    


}
