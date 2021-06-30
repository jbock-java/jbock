package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.narrow;

/**
 * An {@link Optional}, interpreted as an {@code Either<Void, R>}.
 *
 * @param <R> the RHS type
 */
public final class UnbalancedRight<R> extends UnbalancedBase<R> {

  private static final UnbalancedRight<?> EMPTY = new UnbalancedRight<>(Optional.empty());

  private UnbalancedRight(Optional<? extends R> right) {
    super(right);
  }

  static <R> UnbalancedRight<R> of(Optional<? extends R> right) {
    if (right.isEmpty()) {
      return empty();
    }
    return new UnbalancedRight<>(right);
  }

  @SuppressWarnings("unchecked")
  static <L> UnbalancedRight<L> empty() {
    return (UnbalancedRight<L>) EMPTY;
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
    return value.<Either<L, R>>map(Either::right)
        .orElseGet(() -> narrow(choice.get()));
  }
}
