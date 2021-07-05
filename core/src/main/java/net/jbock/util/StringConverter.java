package net.jbock.util;

import io.jbock.util.Either;

import java.util.function.Function;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

/**
 * Converter that converts a string to any arbitrary type.
 * The implementing class must be a public class with no free type variables,
 * and have a parameterless public constructor.
 *
 * @param <T> the type of the conversion result
 */
public abstract class StringConverter<T> implements Function<String, Either<ConverterFailure, T>> {

    /**
     * Converts a single command line token.
     * For options, the token is the option argument.
     * This method will be invoked
     * once per corresponding token in the input array,
     * so it may never be invoked if no such token exists.
     * All corresponding tokens will be handled by the same
     * converter, so this method may be invoked more
     * than once on the same instance, if it is bound to a
     * repeatable item.
     *
     * <p>The implementation is free to throw any {@link Exception}
     * to signal a converter failure.
     * It is an error to return a {@code null} result
     * from this method.
     *
     * @see net.jbock.model.Multiplicity#REPEATABLE
     * @param token a non-null string, possibly empty
     * @return an instance of {@code T}
     * @throws Exception converter failure
     */
    protected abstract T convert(String token) throws Exception;

    /**
     * Creates a {@link StringConverter} from a function.
     *
     * @param function a function that should not return null
     * @param <T> function output type
     * @return converter instance
     */
    public static <T> StringConverter<T> create(Function<String, T> function) {
        return new StringConverter<>() {
            @Override
            public T convert(String token) {
                return function.apply(token);
            }
        };
    }

    @Override
    public final Either<ConverterFailure, T> apply(String s) {
        try {
            T result = convert(s);
            if (result == null) {
                return left(new ConverterReturnedNull());
            }
            return right(result);
        } catch (Exception e) {
            return left(new ConverterThrewException(e));
        }
    }
}
