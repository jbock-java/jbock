package net.jbock.either;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A class that acts as a container for a value of one of two types. An Either
 * will be either be a &quot;Left&quot; or a &quot;Right&quot;,
 * but not &quot;none&quot; or &quot;both&quot;.
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
   * Constructs a Left instance.
   *
   * @param value the LHS value, usually some kind of failure object
   * @param <L> the type of the LHS value
   * @param <R> an arbitrary RHS type
   * @return a Left containing the LHS value
   */
  public static <L, R> Either<L, R> left(L value) {
    return Left.create(value);
  }

  /**
   * Constructs a Right instance.
   *
   * @param value the RHS value
   * @param <L> an arbitrary LHS type
   * @param <R> the type of the RHS value
   * @return a Right containing the RHS value
   */
  public static <L, R> Either<L, R> right(R value) {
    return Right.create(value);
  }

  /**
   * Constructs an unbalanced Left value from an {@link Optional}.
   *
   * @param left an optional LHS value
   * @param <L> the LHS type
   * @return an unbalanced Left, possibly containing an LHS value
   */
  public static <L> UnbalancedLeft<L> unbalancedLeft(Optional<? extends L> left) {
    return new UnbalancedLeft<>(left);
  }

  /**
   * Constructs an unbalanced Right value from an {@link Optional}.
   *
   * @param right an optional RHS value
   * @param <R> the RHS type
   * @return an unbalanced Right, possibly containing an RHS value
   */
  public static <R> UnbalancedRight<R> unbalancedRight(Optional<? extends R> right) {
    return new UnbalancedRight<>(right);
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
   * Collect the RHS values in the stream into a Right,
   * or, if at least one LHS value exists in the stream,
   * return a Left containing one of the LHS values.
   *
   * @param <L> the LHS type
   * @param <R> the RHS type
   * @return a list of the RHS values in the stream,
   *         or, if it exists, one of the LHS values
   */
  public static <L, R> Collector<Either<L, R>, ?, Either<L, List<R>>> toValidList() {
    return new ValidatingCollector<>();
  }

  /**
   * Get the LHS value. The result will be present if and only if the
   * result of {@link #getRight()} is absent.
   *
   * @return the LHS value, or {@link Optional#empty()} if this is a Right
   */
  public abstract Optional<L> getLeft();

  /**
   * Get the RHS value. The result will be present if and only if the
   * result of {@link #getLeft()} is absent.
   *
   * @return the RHS value, or {@link Optional#empty()} if this is a Left
   */
  public abstract Optional<R> getRight();

  @SuppressWarnings("unchecked")
  static <L, R> Either<L, R> narrow(Either<? extends L, ? extends R> either) {
    return (Either<L, R>) either;
  }
}
