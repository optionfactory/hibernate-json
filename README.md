# Hibernate-JSON
Custom Hibernate types for serializing fields as JSON
## Maven dependency configuration

```xml
<dependency>
    <groupId>net.optionfactory</groupId>
    <artifactId>hibernate-json</artifactId>
    <version>1.0</version>
</dependency>
```

## Example usage
### Providing a JSON driver instance through Spring
The default DriverLocator is `@Configurable` and so looks up `JsonDriver` instances in current Spring `ApplicationContext`.
For this to work, you need to use `@EnableSpringConfigured` or define an `AnnotationBeanConfigurerAspect` and `@DependsOn('annotationBeanConfigurerAspect')` on your `DataSource`.

```java
@EnableSpringConfigured
public class SpringConf {
    /* datasource, txManager, SessionFactory, etc */
    @Bean
    public JsonDriver myJsonDriverBeanName() {
        return new JacksonJsonDriver(new ObjectMapper());
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
```

If you have multiple `JsonDriver` instances defined in your application context, you need to specify the name of the one to use:

```java
@Entity
public class EntityWithJsonFields {

    @Id
    @GeneratedValue
    public Integer id;
    
    @Type(type = JsonType.TYPE)
    @JsonType.WithDriver("myJsonDriverBeanName")
    public List<Map<Integer, Set<Long>>> field;
}
```

### Providing a JsonDriver instance without Spring
```java
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
    public JsonDriver locate(Annotation[] fieldAnnotations, Optional<String> name) {
        return driver; 
    }
}
```

### Using hibernate xml mapping (no annotations)
#### With Spring

```xml
<!-- mappings -->
<property name="priority">
    <type name="com.mycompany.usertypes.DefaultValueIntegerType">
        <!-- Only required if more than one driver in ApplicationContext -->    
        <param name="jsonDriverName">myJsonDriverBeanName</param> 
    </type>
</property>
<!-- more mappings -->
```

#### Without Spring

```xml
<!-- mappings -->
<property name="priority">
    <type name="com.mycompany.usertypes.DefaultValueIntegerType">
        <param name="jsonDriverLocatorClass">com.example.MyDriverLocator</param>
    </type>
</property>
<!-- more mappings -->
```
    
