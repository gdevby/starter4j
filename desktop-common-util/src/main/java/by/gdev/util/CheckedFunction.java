package by.gdev.util;
/**
 * Wrapped for streams. You can't generate every time try catch for stream.
 * And used {@link DesktopUtil#wrap(CheckedFunction)} 
 * 
 * @author Robert Makrytski
 *
 * @param <T> generic type
 * @param <R> generic type.
 */
@FunctionalInterface
public interface CheckedFunction<T,R> {
    R apply(T t) throws Exception;
}
