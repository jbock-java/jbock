package net.jbock.util;

import io.jbock.util.Either;

import java.util.function.Function;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

/**
 * Base class for a converter that converts a command line token.
 * An implementation class must be public with no free type variables,
 * and have a parameterless public constructor.
 *
 * @param <T> the type of the conversion result
 */
public abstract class StringConverter<T> implements Function<String, Either<ConverterFailure, T>> {

    private static final StringConverter<String> IDENTITY = new StringConverter<>() {
        @Override
        protected String convert(String token) {
            return token;
        }
    };

    public static StringConverter<String> identity() {
        return IDENTITY;
    }

    /**
     * Converts a single command line token.
     * For options, the token is the option argument.
     * This method will be invoked
     * once per corresponding token in the command line input.
     *
     * <p>The implementation is free to throw any {@link Exception}
     * to signal a converter failure.
     * It is an error to return a {@code null} result
     * from this method.
     *
     * @see net.jbock.model.Multiplicity#REPEATABLE
     * @param token a non-null string
     * @return result of the conversion
     * @throws Exception converter failure
     */
    protected abstract T convert(String token) throws Exception;

    /**
     * Creates a {@link StringConverter} from a {@code Function}.
     *
     * @param function a function that performs string conversion
     * @param <T> output type of the conversion function
     * @return an instance of {@code StringConverter} that converts
     *         by invoking the {@code function}
     */
    public static <T> StringConverter<T> create(Function<String, T> function) {
        return new StringConverter<>() {
            @Override
            public T convert(String token) {
                return function.apply(token);
            }
        };
    }

    /**
     * This method is internal API and should not be used
     * in client code.
     *
     * @param token a non-null string
     * @return conversion result
     */
    @Override
    public final Either<ConverterFailure, T> apply(String token) {
        try {
            T result = convert(token);
            if (result == null) {
                return left(new ConverterReturnedNull());
            }
            return right(result);
        } catch (Exception e) {
            return left(new ConverterThrewException(e));
        }
    }
}
