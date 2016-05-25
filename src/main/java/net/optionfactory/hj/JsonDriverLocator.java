package net.optionfactory.hj;

import java.util.Optional;

/**
 *
 * @author rferranti
 */
public interface JsonDriverLocator {

    JsonDriver locate(Optional<String> name);

}
