package net.jbock.either;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.narrow;

/**
 * A container object which may or may not contain a non-{@code null} value.
 * This class might be used a drop-in replacement for {@link java.util.Optional},
 * but please note the following differences:
 *
 * <ul>
 *   <li>The {@link #map(Function)} method throws an exception if the mapper
 *       function returns a {@code null} result.
 *   <li>There is no {@code get()} method. Use {@link #orElseThrow()} instead.
 *   <li>Some null-checks are omitted. For example, the result of applying
 *       the mapper is not null-checked in {@link #flatMap(Function)}.
 * </ul>
 *
 * <p>Other than the methods that are basically copied from {@code java.util.Optional},
 * this class contains some additional methods related to {@link Either}.
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
   * Returns an {@code Optional} describing the given value, if
   * non-{@code null}, otherwise returns an empty {@code Optional}.
   *
   * @param value the possibly-{@code null} value to describe
   * @param <T> the type of the value
   * @return an {@code Optional} with a present value if the specified value
   *         is non-{@code null}, otherwise an empty {@code Optional}
   */
  public static <T> Optional<T> ofNullable(T value) {
    return value == null ? empty() : of(value);
  }

  /**
   * If a value is present, and the value matches the given predicate,
   * returns an {@code Optional} describing the value, otherwise returns an
   * empty {@code Optional}.
   *
   * @param predicate the predicate to apply to a value, if present
   * @return an {@code Optional} describing the value of this
   *         {@code Optional}, if a value is present and the value matches the
   *         given predicate, otherwise an empty {@code Optional}
   * @throws NullPointerException if the predicate is {@code null}
   */
  public Optional<R> filter(Predicate<? super R> predicate) {
    if (!isPresent()) {
      return this;
    }
    return predicate.test(orElseThrow()) ? this : empty();
  }

  /**
   * If a value is present, returns an {@code Optional} describing
   * the result of applying the given mapping function to
   * the value, otherwise returns an empty {@code Optional}.
   *
   * <p>If the mapping function returns a {@code null} result,
   * then this method throws a {@code NullPointerException}.
   *
   * @param mapper the mapping function to apply to a value, if present
   * @param <R2> The type of the value returned from the mapping function
   * @return an {@code Optional} describing the result of applying a mapping
   *         function to the value of this {@code Optional}, if a value is
   *         present, otherwise an empty {@code Optional}
   * @throws NullPointerException if the mapping function returns {@code null}
   */
  public <R2> Optional<R2> map(Function<? super R, ? extends R2> mapper) {
    if (!isPresent()) {
      return empty();
    }
    return Optional.of(mapper.apply(orElseThrow()));
  }

  /**
   * If a value is present, returns the result of applying the given
   * {@code Optional}-bearing mapping function to the value, otherwise returns
   * an empty {@code Optional}.
   *
   * @param <R2> The type of value of the {@code Optional} returned by the
   *             mapping function
   * @param mapper the mapping function to apply to a value, if present
   * @return the result of applying the mapping function to the value, if a value
   *         is present, otherwise an empty {@code Optional}
   */
  public <R2> Optional<R2> flatMap(Function<? super R, Optional<? extends R2>> mapper) {
    if (!isPresent()) {
      return empty();
    }
    @SuppressWarnings("unchecked")
    Optional<R2> result = (Optional<R2>) mapper.apply(orElseThrow());
    return result;
  }

  /**
   * If a value is present, returns an {@code Optional} describing the value,
   * otherwise returns an {@code Optional} produced by the supplying function.
   *
   * @param supplier the supplying function that produces an {@code Optional}
   *        to be returned
   * @return returns an {@code Optional} describing the value of this
   *         {@code Optional}, if a value is present, otherwise an
   *         {@code Optional} produced by the supplying function.
   */
  public Optional<R> or(Supplier<? extends Optional<? extends R>> supplier) {
    if (isPresent()) {
      return this;
    }
    @SuppressWarnings("unchecked")
    Optional<R> result = (Optional<R>) supplier.get();
    return result;
  }

  /**
   * If a value is present, returns a Right-{@link Either}
   * containing that value.
   * Otherwise returns a Left-Either containing the supplied value.
   *
   * @param left supplier of a Left value
   * @param <L> the LHS type
   * @return a Right-Either containing the value, if it exists,
   *         or otherwise a Left-Either containing the result of invoking {@code left.get()}
   */
  public <L> Either<L, R> orElseLeft(Supplier<? extends L> left) {
    return flatMapLeft(() -> left(left.get()));
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
      return Either.right(orElseThrow());
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
        ? String.format("Optional[%s]", orElseThrow())
        : "Optional.empty";
  }

  /**
   * Indicates whether some other object is "equal to" this {@code Optional}.
   *
   * @param obj an object to be tested for equality
   * @return {@code true} if the other object is "equal to" this object
   *         otherwise {@code false}
   */
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
