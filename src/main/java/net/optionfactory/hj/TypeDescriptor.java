package net.optionfactory.hj;

/**
 * 
 * @author rferranti
 */
public interface TypeDescriptor {

    <T> T as(Class<T> cls);

    Class<?> rawClass();
    
}
