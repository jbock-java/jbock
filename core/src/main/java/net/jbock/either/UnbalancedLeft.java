package net.jbock.either;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.narrow;
import static net.jbock.either.Either.right;

/**
 * A container object which may or may not contain a non-{@code null} Left value.
 *
 * @param <L> the type of the Left value
 */
public final class UnbalancedLeft<L> extends UnbalancedBase<L> {

  private static final UnbalancedLeft<?> EMPTY = new UnbalancedLeft<>(null);

  private UnbalancedLeft(L left) {
    super(left);
  }

  /**
   * Returns an {@code UnbalancedRight} containing the given
   * non-{@code null} value.
   *
   * @param left the value, which must be non-{@code null}
   * @param <L> the type of the value
   * @return an {@code UnbalancedLeft} with the value present
   * @throws NullPointerException if value is {@code null}
   */
  public static <L> UnbalancedLeft<L> of(L left) {
    return new UnbalancedLeft<>(Objects.requireNonNull(left));
  }

  /**
   * Returns an empty instance.
   *
   * @param <L> type of the non-existent value
   * @return an empty {@code UnbalancedLeft}
   */
  @SuppressWarnings("unchecked")
  public static <L> UnbalancedLeft<L> empty() {
    return (UnbalancedLeft<L>) EMPTY;
  }

  /**
   * Creates a &quot;balanced&quot; instance by providing an alternative
   * Right value.
   *
   * @param right supplier of a Right value
   * @param <R> the RHS type
   * @return a Left containing the {@code left} value if it is present,
   *         or otherwise a Right the result of invoking {@code right.get()}
   */
  public <R> Either<L, R> orElseRight(Supplier<? extends R> right) {
    return flatMap(() -> right(right.get()));
  }

  /**
   * Creates a &quot;balanced&quot; instance containing the Left value,
   * or throws a runtime exception if the Left value is not present.
   *
   * @param <R> an arbitrary RHS type
   * @return an Either containing the Left value
   * @throws NoSuchElementException – if no Left value is present
   */
  public <R> Either<L, R> orElseThrow() {
    return Either.left(unsafeGet());
  }

  /**
   * If the {@code left} value is absent, invoke the supplied Supplier
   * to create a balanced instance.
   * Otherwise return a balanced Left.
   *
   * @param choice a choice function
   * @param <R> the RHS type
   * @return an equivalent instance if this is a Left, otherwise the result of
   *         invoking {@code choice}
   */
  public <R> Either<L, R> flatMap(
      Supplier<? extends Either<? extends L, ? extends R>> choice) {
    if (isPresent()) {
      return left(unsafeGet());
    }
    return narrow(choice.get());
  }

  /**
   * If a value is present, returns the result of applying the given
   * mapping function to the value, otherwise returns
   * an empty {@code UnbalancedLeft}.
   *
   * @param <L2> The type of value of the {@code UnbalancedLeft} returned by the
   *            mapping function
   * @param mapper the mapping function to apply to a value, if present
   * @return the result of applying an {@code UnbalancedLeft}-bearing mapping
   *         function to the value of this {@code UnbalancedLeft}, if a value is
   *         present, otherwise an empty {@code UnbalancedLeft}
   */
  public <L2> UnbalancedLeft<L2> flatMapLeft(Function<? super L, UnbalancedLeft<? extends L2>> mapper) {
    if (isEmpty()) {
      return empty();
    }
    @SuppressWarnings("unchecked")
    UnbalancedLeft<L2> result = (UnbalancedLeft<L2>) mapper.apply(unsafeGet());
    return result;
  }

  /**
   * If a value is present, returns an {@code UnbalancedLeft} containing
   * the result of applying the given mapping function to
   * the value, otherwise returns an empty {@code UnbalancedLeft}.
   *
   * <p>If the mapping function returns a {@code null} result then this method
   * throws a {@code NullPointerException}.
   *
   * @param mapper the mapping function to apply to a value, if present
   * @param <L2> The type of the value returned from the mapping function
   * @return an {@code UnbalancedLeft} describing the result of applying a mapping
   *         function to the value, if a value is
   *         present, otherwise an empty {@code UnbalancedLeft}
   * @throws NullPointerException if the mapping function returns {@code null}
   */
  public <L2> UnbalancedLeft<L2> mapLeft(Function<? super L, ? extends L2> mapper) {
    if (isEmpty()) {
      return empty();
    }
    return of(mapper.apply(unsafeGet()));
  }

  /**
   * Returns a string representation of this {@code UnbalancedLeft}
   * suitable for debugging. The exact presentation format is unspecified and
   * may vary between implementations and versions.
   *
   * @return the string representation of this instance
   */
  @Override
  public String toString() {
    return isPresent()
        ? String.format("UnbalancedLeft[%s]", unsafeGet())
        : "UnbalancedLeft.empty";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof UnbalancedLeft)) {
      return false;
    }

    UnbalancedLeft<?> other = (UnbalancedLeft<?>) obj;
    return isEqual(other);
  }
}
