package net.jbock.util;

import net.jbock.either.Either;

import java.util.function.Function;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

/**
 * Converts strings to any arbitrary type.
 * The implementing class must be public
 * and have a public default constructor.
 *
 * @param <T> converter output type
 */
public abstract class StringConverter<T> implements Function<String, Either<ConverterFailure, T>> {

  /**
   * Converts a single command line token. This method will be invoked
   * if the corresponding token was present in the input array.
   * Conversion failure is signaled by throwing any exception.
   * It is an error to return {@code null} from this method.
   *
   * @param token a non-null string
   * @return a non-null instance of {@code T}
   * @throws Exception if a parsing error occurs
   */
  public abstract T convert(String token) throws Exception;

  /**
   * Factory method to create a {@link StringConverter} from a function.
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
