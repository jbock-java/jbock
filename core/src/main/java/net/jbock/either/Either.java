package net.jbock.either;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A class that acts as a container for a value of one of two types. An Either
 * will be either be a "Left" or a "Right",
 * but not "none" or "both".
 * An Either can be used to express a success or failure case. By convention,
 * Right is used to store a success value, and Left is used to store a failure
 * value.
 *
 * @param <L> the type of the LHS value
 * @param <R> the type of the RHS value
 */
public abstract class Either<L, R> {

  Either() {
  }

  /**
   * Constructs a Left instance containing the given
   * non-{@code null} value.
   *
   * @param value the LHS value, usually some kind of failure object
   * @param <L> the type of the LHS value
   * @param <R> an arbitrary RHS type
   * @return a Left containing the LHS value
   * @throws NullPointerException if value is {@code null}
   */
  public static <L, R> Either<L, R> left(L value) {
    return Left.create(Objects.requireNonNull(value));
  }

  /**
   * Constructs a Right instance containing the given
   * non-{@code null} value.
   *
   * @param value the RHS value
   * @param <L> an arbitrary LHS type
   * @param <R> the type of the RHS value
   * @return a Right containing the RHS value
   * @throws NullPointerException if value is {@code null}
   */
  public static <L, R> Either<L, R> right(R value) {
    return Right.create(Objects.requireNonNull(value));
  }

  /**
   * Collect the RHS values in the stream into a Right,
   * or, if the stream contains an LHS value,
   * return a Left containing the first such value.
   *
   * @param <L> the LHS type
   * @param <R> the RHS type
   * @return a Right containing all RHS values in the stream,
   *         or, if an LHS value exists, a Left containing the first such value
   */
  public static <L, R> Collector<Either<L, R>, ?, Either<L, List<R>>> toValidList() {
    return new ValidatingCollector<>();
  }

  /**
   * Collect the RHS values in the stream into a Right,
   * or, if the stream an LHS value,
   * return a Left containing all LHS values in the original order.
   *
   * @param <L> the LHS type
   * @param <R> the RHS type
   * @return a list of the RHS values in the stream,
   *         or, if an LHS value exists, a nonempty list of all LHS values
   */
  public static <L, R> Collector<Either<L, R>, ?, Either<List<L>, List<R>>> toValidListAll() {
    return new ValidatingCollectorAll<>();
  }

  /**
   * Apply the supplied function to the RHS value if this is a Right,
   * otherwise return an equivalent Left instance with an updated RHS type.
   *
   * @param rightMapper the function to apply to the RHS value, if this is a Right
   * @param <R2> the new RHS type
   * @return an equivalent instance if this is a Left, otherwise a Right containing
   *         the result of applying {@code rightMapper} to the RHS value
   */
  public final <R2> Either<L, R2> map(Function<? super R, ? extends R2> rightMapper) {
    return flatMap(r -> right(rightMapper.apply(r)));
  }

  /**
   * Apply the supplied function to the RHS value if this is a Right,
   * otherwise return an equivalent Left instance with an updated RHS type.
   *
   * @param choice a choice function
   * @param <R2> the new RHS type
   * @return an equivalent instance if this is a Left, otherwise the result of
   *         applying {@code choice} to the RHS value
   */
  public final <R2> Either<L, R2> flatMap(
      Function<? super R, ? extends Either<? extends L, ? extends R2>> choice) {
    return fold(Either::left, r -> narrow(choice.apply(r)));
  }

  /**
   * If this is a Right, and the result of applying {@code test}
   * contains a value, return a Left containing that value.
   * Otherwise return an equivalent instance.
   *
   * @param test the filter function
   * @return filter result
   */
  public final Either<L, R> filter(Function<? super R, LeftOptional<? extends L>> test) {
    return flatMap(r -> test.apply(r).orElseRight(() -> r));
  }

  /**
   * Apply the supplied function to the LHS value if this is a Left,
   * otherwise return an equivalent Right instance with an updated LHS type.
   *
   * @param leftMapper the function to apply to the LHS value, if one exists
   * @param <L2> the new LHS type
   * @return an equivalent instance if this is a Right, otherwise a Left containing
   *         the result of applying {@code leftMapper} to the LHS value
   */
  public final <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> leftMapper) {
    return narrow(flip().map(leftMapper).flip());
  }

  /**
   * Apply the supplied function to the LHS value if this is a Left,
   * otherwise return an equivalent Right instance with an updated LHS type.
   *
   * @param choice a choice function
   * @param <L2> the new LHS type
   * @return an equivalent instance if this is a Right, otherwise the result of
   *         applying {@code choice} to the LHS value
   */
  public final <L2> Either<L2, R> flatMapLeft(
      Function<? super L, ? extends Either<? extends L2, ? extends R>> choice) {
    return narrow(flip().flatMap(l -> choice.apply(l).flip()).flip());
  }

  /**
   * Throws the supplied exception if this is a Left, otherwise
   * return the RHS value.
   *
   * @param f exception supplier
   * @param <X> type of the exception
   * @return the RHS value, if this is a Right
   * @throws X the result of applying {@code f} to the LHS value, if this is a Left
   */
  public abstract <X extends Throwable> R orElseThrow(Function<? super L, ? extends X> f) throws X;

  /**
   * Applies the function to the wrapped value, applying {@code leftMapper}
   * if this is a Left and {@code rightMapper} if this is a Right.
   *
   * @param leftMapper the function to apply if this is a Left
   * @param rightMapper the function to apply if this is a Right
   * @param <U> result type
   * @return the result of applying either {@code leftMapper} or {@code rightMapper}
   */
  public abstract <U> U fold(
      Function<? super L, ? extends U> leftMapper,
      Function<? super R, ? extends U> rightMapper);

  abstract Either<R, L> flip();

  /**
   * Applies either the left action or the right action.
   *
   * @param leftAction action to run if this is a Left
   * @param rightAction action to run if this is a Right
   */
  public final void accept(Consumer<? super L> leftAction, Consumer<? super R> rightAction) {
    fold(l -> {
      leftAction.accept(l);
      return null;
    }, r -> {
      rightAction.accept(r);
      return null;
    });
  }

  /**
   * Apply the left action if this is a Left.
   * Otherwise do nothing.
   *
   * @param leftAction action to run if this is a Left
   */
  public final void acceptLeft(Consumer<? super L> leftAction) {
    accept(leftAction, r -> {
    });
  }

  /**
   * Apply the right action if this is a Right.
   * Otherwise do nothing.
   *
   * @param rightAction action to run if this is a Right
   */
  public final void acceptRight(Consumer<? super R> rightAction) {
    accept(l -> {
    }, rightAction);
  }

  /**
   * Get the LHS value. The result will be present if and only if the
   * result of {@link #getRight()} is absent.
   *
   * @return the LHS value, or {@link java.util.Optional#empty()} if this is a Right
   */
  public abstract LeftOptional<L> getLeft();


  /**
   * If this is a Left, returns true, otherwise false.
   *
   * @return {@code true} if this is a Left, otherwise {@code false}
   */
  public abstract boolean isLeft();

  /**
   * If this is a Right, returns true, otherwise false.
   *
   * @return {@code true} if this is a Right, otherwise {@code false}
   */
  public final boolean isRight() {
    return !isLeft();
  }

  /**
   * Get the RHS value. The result will be present if and only if the
   * result of {@link #getLeft()} is absent.
   *
   * @return the RHS value, or {@link java.util.Optional#empty()} if this is a Left
   */
  public abstract Optional<R> getRight();

  @SuppressWarnings("unchecked")
  static <L, R> Either<L, R> narrow(Either<? extends L, ? extends R> either) {
    return (Either<L, R>) either;
  }

  /**
   * Returns a string representation of this {@code Either}
   * suitable for debugging.  The exact presentation format is unspecified and
   * may vary between implementations and versions.
   *
   * @return the string representation of this instance
   */
  @Override
  public abstract String toString();
}
