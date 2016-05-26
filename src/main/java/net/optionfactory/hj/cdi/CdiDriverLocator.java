package net.optionfactory.hj.cdi;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Qualifier;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.JsonDriverLocator;
import net.optionfactory.hj.JsonDriverNotFound;
import static net.optionfactory.hj.UserTypes.searchAnnotation;

public class CdiDriverLocator implements JsonDriverLocator {

    @Override
    public JsonDriver locate(Annotation[] fieldAnnotations, Optional<String> name) {
        final BeanManager bm = CDI.current().getBeanManager();        
        final Annotation[] qualifiers = Arrays.stream(fieldAnnotations).filter(a -> searchAnnotation(a, Qualifier.class).isPresent()).toArray(n -> new Annotation[n]);
        JsonDriverNotFound.failIf(qualifiers.length > 0 && name.isPresent(), "found both @Qualifiers and @WithDriver.value()");
        final Set<Bean<?>> beans = name.map(n -> bm.getBeans(n)).orElseGet(() -> bm.getBeans(JsonDriver.class, qualifiers));
        JsonDriverNotFound.failIf(beans.isEmpty(), "no JsonDriver found in BeanManager");
        JsonDriverNotFound.failIf(beans.size() > 1, "more than one JsonDriver found in BeanManager, use @JsonType.WithDriver to disambiguate");
        final Bean<?> bean = beans.iterator().next();
        final CreationalContext<?> ctx = bm.createCreationalContext(bean);
        return (JsonDriver) bm.getReference(bean, JsonDriver.class, ctx);	        
    }
    
}
