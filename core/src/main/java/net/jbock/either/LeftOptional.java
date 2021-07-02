package net.jbock.either;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.narrow;
import static net.jbock.either.Either.right;

/**
 * A container object which may or may not contain a non-{@code null} value.
 * This class might be used as a drop-in replacement for {@link java.util.Optional},
 * in situations where the presence of a value should indicate some sort of failure.
 * Please note the following differences:
 *
 * <ul>
 *   <li>The {@link #map(Function)} method throws an exception if the mapper
 *       function returns a {@code null} result.
 *   <li>There is no {@code get()} method. Use {@link #orElseThrow()} instead.
 * </ul>
 *
 * <p>In addition to the methods that are basically copied from {@code java.util.Optional},
 * this class contains some methods related to {@link Either},
 * in particular {@link #orElseRight(Supplier)} and {@link #flatMapRight(Supplier)}.
 *
 * @param <L> the type of the value
 */
public final class LeftOptional<L> extends AbstractOptional<L> {

  private static final LeftOptional<?> EMPTY = new LeftOptional<>(null);

  private LeftOptional(L value) {
    super(value);
  }

  /**
   * Returns a {@code LeftOptional} containing the given
   * non-{@code null} value.
   *
   * @param value the value, which must be non-{@code null}
   * @param <L> the type of the value
   * @return a {@code LeftOptional} with the value present
   * @throws NullPointerException if value is {@code null}
   */
  public static <L> LeftOptional<L> of(L value) {
    return new LeftOptional<>(Objects.requireNonNull(value));
  }

  /**
   * Returns an empty instance.
   *
   * @param <L> type of the non-existent value
   * @return an empty {@code LeftOptional}
   */
  @SuppressWarnings("unchecked")
  public static <L> LeftOptional<L> empty() {
    return (LeftOptional<L>) EMPTY;
  }

  /**
   * Returns an {@code LeftOptional} describing the given value, if
   * non-{@code null}, otherwise returns an empty {@code LeftOptional}.
   *
   * @param value the possibly-{@code null} value to describe
   * @param <L> the type of the value
   * @return an {@code LeftOptional} with a present value if the specified value
   *         is non-{@code null}, otherwise an empty {@code LeftOptional}
   */
  public static <L> LeftOptional<L> ofNullable(L value) {
    return value == null ? empty() : of(value);
  }

  /**
   * If a value is present, and the value matches the given predicate,
   * returns a {@code LeftOptional} describing the value, otherwise returns an
   * empty {@code LeftOptional}.
   *
   * @param predicate the predicate to apply to a value, if present
   * @return a {@code LeftOptional} describing the value of this
   *         {@code LeftOptional}, if a value is present and the value matches the
   *         given predicate, otherwise an empty {@code LeftOptional}
   * @throws NullPointerException if the predicate is {@code null}
   */
  public LeftOptional<L> filter(Predicate<? super L> predicate) {
    if (!isPresent()) {
      return this;
    }
    return predicate.test(orElseThrow()) ? this : empty();
  }

  /**
   * If a value is present, returns an {@code LeftOptional} containing
   * the result of applying the given mapping function to
   * the value, otherwise returns an empty {@code LeftOptional}.
   *
   * <p>If the mapping function returns a {@code null} result then this method
   * throws a {@code NullPointerException}.
   *
   * @param mapper the mapping function to apply to a value, if present
   * @param <L2> The type of the value returned from the mapping function
   * @return a {@code LeftOptional} describing the result of applying a mapping
   *         function to the value, if a value is
   *         present, otherwise an empty {@code LeftOptional}
   * @throws NullPointerException if the mapping function returns {@code null}
   */
  public <L2> LeftOptional<L2> map(Function<? super L, ? extends L2> mapper) {
    if (isEmpty()) {
      return empty();
    }
    return of(mapper.apply(orElseThrow()));
  }

  /**
   * If a value is present, returns the result of applying the given
   * mapping function to the value, otherwise returns
   * an empty {@code LeftOptional}.
   *
   * @param <L2> The type of value of the {@code LeftOptional} returned by the
   *             mapping function
   * @param mapper the mapping function to apply to a value, if present
   * @return the result of applying the mapping function to the value, if a value
   *         is present, otherwise an empty {@code LeftOptional}
   */
  public <L2> LeftOptional<L2> flatMap(Function<? super L, LeftOptional<? extends L2>> mapper) {
    if (isEmpty()) {
      return empty();
    }
    @SuppressWarnings("unchecked")
    LeftOptional<L2> result = (LeftOptional<L2>) mapper.apply(orElseThrow());
    return result;
  }

  /**
   * If a value is present, returns an {@code LeftOptional} describing the value,
   * otherwise returns a {@code LeftOptional} produced by the supplier.
   *
   * @param supplier the supplier that produces a {@code LeftOptional}
   *        to be returned
   * @return returns an {@code LeftOptional} containing the value of this
   *         {@code LeftOptional}, if a value is present, otherwise a
   *         {@code LeftOptional} produced by the supplier.
   */
  public LeftOptional<L> or(Supplier<? extends LeftOptional<? extends L>> supplier) {
    if (isPresent()) {
      return this;
    }
    @SuppressWarnings("unchecked")
    LeftOptional<L> result = (LeftOptional<L>) supplier.get();
    return result;
  }

  /**
   * If a value is present, returns a Left-{@link Either}
   * containing that value.
   * Otherwise returns a Right-Either containing the value produced
   * by the supplier.
   *
   * @param supplier supplier of a Right value
   * @param <R> the RHS type
   * @return if the value is present, a Left containing that value,
   *         otherwise a Right containing the result of invoking {@code supplier.get()}
   */
  public <R> Either<L, R> orElseRight(Supplier<? extends R> supplier) {
    return flatMapRight(() -> right(supplier.get()));
  }

  /**
   * If a value is present, return a Left-{@link Either} containing
   * that value. Otherwise returns the Either instance produced by
   * the supplier.
   *
   * @param supplier a supplier that yields an Either instance
   * @param <R> the RHS type
   * @return a Left-Either containing the value, or if no value is present,
   *         the result of invoking {@code supplier.get()}
   */
  public <R> Either<L, R> flatMapRight(
      Supplier<? extends Either<? extends L, ? extends R>> supplier) {
    if (isPresent()) {
      return left(orElseThrow());
    }
    return narrow(supplier.get());
  }

  /**
   * Returns a string representation of this {@code LeftOptional}
   * suitable for debugging. The exact presentation format is unspecified and
   * may vary between implementations and versions.
   *
   * @return the string representation of this instance
   */
  @Override
  public String toString() {
    return isPresent()
        ? String.format("OptionalLeft[%s]", orElseThrow())
        : "Optional.empty";
  }

  /**
   * Indicates whether some other object is "equal to" this {@code LeftOptional}.
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

    if (!(obj instanceof LeftOptional)) {
      return false;
    }

    LeftOptional<?> other = (LeftOptional<?>) obj;
    return isEqual(other);
  }
}
