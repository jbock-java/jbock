package net.jbock.either;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.narrow;

/**
 * A container object which may or may not contain a non-{@code null} Right value.
 *
 * @param <R> the type of the Right value
 */
public final class UnbalancedRight<R> extends UnbalancedBase<R> {

  private static final UnbalancedRight<?> EMPTY = new UnbalancedRight<>(null);

  private UnbalancedRight(R right) {
    super(right);
  }

  /**
   * Returns an {@code UnbalancedRight} containing the given
   * non-{@code null} value.
   *
   * @param right the value, which must be non-{@code null}
   * @param <R> the type of the value
   * @return an {@code UnbalancedLeft} with the value present
   * @throws NullPointerException if value is {@code null}
   */
  public static <R> UnbalancedRight<R> of(R right) {
    return new UnbalancedRight<>(Objects.requireNonNull(right));
  }

  /**
   * Returns an empty instance.
   *
   * @param <R> type of the non-existent value
   * @return an empty {@code UnbalancedRight}
   */
  @SuppressWarnings("unchecked")
  public static <R> UnbalancedRight<R> empty() {
    return (UnbalancedRight<R>) EMPTY;
  }

  /**
   * Creates a &quot;balanced&quot; instance by providing an alternative
   * Left value.
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
   * Creates a &quot;balanced&quot; instance containing the Right value,
   * or throws a runtime exception if the Right value is not present.
   *
   * @param <L> an arbitrary LHS type
   * @return an Either containing the Right value
   * @throws NoSuchElementException – if no Right value is present
   */
  public <L> Either<L, R> orElseThrow() {
    return Either.right(unsafeGet());
  }

  /**
   * If the {@code right} value is absent, invoke the supplied Supplier
   * to create a balanced instance.
   * Otherwise return a balanced Right.
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
   * Returns a string representation of this {@code UnbalancedRight}
   * suitable for debugging.  The exact presentation format is unspecified and
   * may vary between implementations and versions.
   *
   * @return the string representation of this instance
   */
  @Override
  public String toString() {
    return isPresent()
        ? String.format("UnbalancedRight[%s]", unsafeGet())
        : "UnbalancedRight.empty";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof UnbalancedRight)) {
      return false;
    }

    UnbalancedRight<?> other = (UnbalancedRight<?>) obj;
    return isEqual(other);
  }
}
