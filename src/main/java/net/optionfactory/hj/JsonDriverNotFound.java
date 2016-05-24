package net.optionfactory.hj;

/**
 *
 * @author rferranti
 */
public class JsonDriverNotFound extends IllegalStateException {

    public static void failIf(boolean fail, String msg) {
        if (fail) {
            throw new JsonDriverNotFound(msg);
        }
    }

    public JsonDriverNotFound(String s) {
        super(s);
    }

}
