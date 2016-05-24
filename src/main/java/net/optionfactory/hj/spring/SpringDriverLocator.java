package net.optionfactory.hj.spring;

import java.util.Map;
import java.util.Optional;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.JsonDriverNotFound;
import net.optionfactory.hj.JsonType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Configurable
public class SpringDriverLocator implements JsonType.DriverLocator, ApplicationContextAware {

    private ApplicationContext ac;

    @Override
    public JsonDriver locate(Optional<String> driverName) {
        JsonDriverNotFound.failIf(ac == null, "null ApplicationContext in JsonType. This class is @Configurable. Use @EnableSpringConfigured or define an AnnotationBeanConfigurerAspect and @DependsOn('annotationBeanConfigurerAspect') on your datasource");
        final Map<String, JsonDriver> matchingBeans = ac.getBeansOfType(JsonDriver.class);
        JsonDriverNotFound.failIf(matchingBeans.isEmpty(), "no JsonDriver found in ApplicationContext");
        if (driverName.isPresent()) {
            JsonDriverNotFound.failIf(!matchingBeans.containsKey(driverName.get()), String.format("no JsonDriver named '%s' in ApplicationContext", driverName.get()));
            return matchingBeans.get(driverName.get());
        }
        JsonDriverNotFound.failIf(matchingBeans.size() > 1, "more than one JsonDriver found in ApplicationContext, use @JsonType.WithDriver to disambiguate");
        return matchingBeans.values().iterator().next();
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.ac = ac;
    }
    
}
