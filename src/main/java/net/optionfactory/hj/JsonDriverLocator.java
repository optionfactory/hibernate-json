package net.optionfactory.hj;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 *
 * @author rferranti
 */
public interface JsonDriverLocator {

    JsonDriver locate(Annotation[] fieldAnnotations, Optional<String> name);

}
