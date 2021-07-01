package net.jbock.either;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.narrow;

/**
 * A container object which may or may not contain a non-{@code null} value.
 *
 * @param <R> the type of the value
 */
public final class Optional<R> extends AbstractOptional<R> {

  private static final Optional<?> EMPTY = new Optional<>(null);

  private Optional(R right) {
    super(right);
  }

  /**
   * Returns an {@code Optional} containing the given
   * non-{@code null} value.
   *
   * @param right the value, which must be non-{@code null}
   * @param <R> the type of the value
   * @return an {@code Optional} with the value present
   * @throws NullPointerException if value is {@code null}
   */
  public static <R> Optional<R> of(R right) {
    return new Optional<>(Objects.requireNonNull(right));
  }

  /**
   * Returns an empty instance.
   *
   * @param <R> type of the non-existent value
   * @return an empty {@code Optional}
   */
  @SuppressWarnings("unchecked")
  public static <R> Optional<R> empty() {
    return (Optional<R>) EMPTY;
  }

  /**
   * If a value is present, returns a Right-{@link Either}
   * containing that value.
   * Otherwise returns a Left-Either containing the supplied value.
   *
   * @param left supplier of a Left value
   * @param <L> the LHS type
   * @return a Right containing the {@code right} value if it is present,
   *         or otherwise a Left containing the result of invoking {@code left.get()}
   */
  public <L> Either<L, R> orElseLeft(Supplier<? extends L> left) {
    return flatMapLeft(() -> left(left.get()));
  }

  /**
   * If a value is present, returns a Right-{@link Either} containing the value,
   * otherwise throws a runtime exception.
   *
   * @param <L> an arbitrary LHS type
   * @return a Right containing the value
   * @throws NoSuchElementException – if no value is present
   */
  public <L> Either<L, R> orElseThrow() {
    return Either.right(unsafeGet());
  }

  /**
   * If a value is present, return a Right-{@link Either} containing
   * that value. Otherwise return the supplied Either instance.
   *
   * @param choice a choice function
   * @param <L> the LHS type
   * @return an equivalent instance if this is a Right, otherwise the result of
   *         invoking {@code choice}
   */
  public <L> Either<L, R> flatMapLeft(
      Supplier<? extends Either<? extends L, ? extends R>> choice) {
    if (isPresent()) {
      return Either.right(unsafeGet());
    }
    return narrow(choice.get());
  }

  /**
   * Returns a string representation of this {@code Optional}
   * suitable for debugging.  The exact presentation format is unspecified and
   * may vary between implementations and versions.
   *
   * @return the string representation of this instance
   */
  @Override
  public String toString() {
    return isPresent()
        ? String.format("Optional[%s]", unsafeGet())
        : "Optional.empty";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Optional)) {
      return false;
    }

    Optional<?> other = (Optional<?>) obj;
    return isEqual(other);
  }
}
