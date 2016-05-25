# Hibernate-JSON
Custom Hibernate types for serializing fields as JSON
## Maven dependency configuration
    <dependency>
        <groupId>net.optionfactory</groupId>
        <artifactId>hibernate-json</artifactId>
        <version>1.0</version>
    </dependency>

## Example usage
### Providing a JSON driver instance through Spring
The default DriverLocator is @Configurable and so looks up JsonDriver instances in current Spring ApplicationContext.
For this to work, you need to use @EnableSpringConfigured or define an AnnotationBeanConfigurerAspect and @DependsOn('annotationBeanConfigurerAspect') on your datasource.

    @EnableSpringConfigured
    public static class SpringConf {
        /* datasource, txManager, SessionFactory, etc */
        @Bean
        public JsonDriver myJsonDriverBeanName() {
            return new JackJsonDriver(new Gson());
        }
    }
    
    @Entity
    public class EntityWithJsonFields {

        @Id
        @GeneratedValue
        public Integer id;
        
        @Type(type = JsonType.TYPE)
        public List<Map<Integer, Set<Long>>> field;
    }
If you have multiple JSON driver instances defined in your application context, you need to specify the name of the one to use:

    @Entity
    public class EntityWithJsonFields {

        @Id
        @GeneratedValue
        public Integer id;
        
        @Type(type = JsonType.TYPE)
        @JsonType.WithDriver("myJsonDriverBeanName")
        public List<Map<Integer, Set<Long>>> field;
    }
    
### Providing a JSON driver instance without Spring
    @Entity
    public class EntityWithJsonFields {

        @Id
        @GeneratedValue
        public Integer id;
        
        @Type(type = JsonType.TYPE)
        @JsonType.WithDriver(locator=MyDriverLocator.class)
        public List<Map<Integer, Set<Long>>> field;
    }
    
    public class MyDriverLocator implements JsonType.DriverLocator {
        private static JsonDriver driver = new JacksonJsonDriver(new ObjectMapper());
        public JsonDriver locate(Optional<String> name) {
            return driver; 
        }
    }

### Using hibernate xml mapping (no annotations)
#### With Spring
    <...>
    <property name="priority">
        <type name="com.mycompany.usertypes.DefaultValueIntegerType">
            <param name="net.optionfactory.hj.driver">myJsonDriverBeanName</param> <!-- Only required if more than one driver in ApplicationContext -->
        </type>
    </property>
    <...>

#### Without Spring
    <...>
    <property name="priority">
        <type name="com.mycompany.usertypes.DefaultValueIntegerType">
            <param name="net.optionfactory.hj.locator">com.example.MyDriverLocator</param>
        </type>
    </property>
    <...>
    
    
