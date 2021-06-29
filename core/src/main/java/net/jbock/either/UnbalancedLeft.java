package net.jbock.either;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.either.Either.narrow;
import static net.jbock.either.Either.right;

/**
 * An {@link Optional}, interpreted as an {@code Either<L, Void>}.
 *
 * @param <L> the LHS type
 */
public final class UnbalancedLeft<L> {

  private static final UnbalancedLeft<?> EMPTY = new UnbalancedLeft<>(Optional.empty());

  private final Optional<? extends L> left;

  private UnbalancedLeft(Optional<? extends L> left) {
    this.left = left;
  }

  static <L> UnbalancedLeft<L> of(Optional<? extends L> left) {
    if (left.isEmpty()) {
      return empty();
    }
    return new UnbalancedLeft<>(left);
  }

  @SuppressWarnings("unchecked")
  static <L> UnbalancedLeft<L> empty() {
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
   * @param <R> the RHS type
   * @return a Left containing the {@code left} value
   * @throws NoSuchElementException – if no value is present
   */
  public <R> Either<L, R> orElseThrow() {
    return Either.left(left.orElseThrow());
  }

  /**
   * If a Left value is present, returns true, otherwise false.
   *
   * @return {@code true} if a value is present, otherwise {@code false}
   */
  public boolean isPresent() {
    return left.isPresent();
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
    return left.<Either<L, R>>map(Either::left)
        .orElseGet(() -> narrow(choice.get()));
  }
}
