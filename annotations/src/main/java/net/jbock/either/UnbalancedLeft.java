package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.either.Either.narrow;
import static net.jbock.either.Either.right;

public final class UnbalancedLeft<L> {

  private final Optional<? extends L> left;

  UnbalancedLeft(Optional<? extends L> left) {
    this.left = left;
  }

  public <R> Either<L, R> orElseRight(Supplier<? extends R> right) {
    return flatMap(() -> right(right.get()));
  }

  public <R> Either<L, R> flatMap(
      Supplier<? extends Either<? extends L, ? extends R>> choice) {
    return left.<Either<L, R>>map(Either::left)
        .orElseGet(() -> narrow(choice.get()));
  }
}
