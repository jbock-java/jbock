package net.jbock;

import net.jbock.either.Either;

import java.util.function.Function;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

/**
 * Converts strings to any arbitrary type.
 * The implementing class must be public, and have a public default constructor.
 */
public abstract class StringConverter<T> implements Function<String, Either<String, T>> {

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

  public static <T> StringConverter<T> create(Function<String, T> function) {
    return new StringConverter<>() {
      @Override
      public T convert(String token) {
        return function.apply(token);
      }
    };
  }

  @Override
  public final Either<String, T> apply(String s) {
    try {
      T result = convert(s);
      if (result == null) {
        return left("converter returned null");
      }
      return right(result);
    } catch (Exception e) {
      return left(e.getMessage());
    }
  }
}
