package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.narrow;

public final class HalfRight<R> {

  private final Optional<? extends R> right;

  HalfRight(Optional<? extends R> right) {
    this.right = right;
  }

  public <L> Either<L, R> orElseLeft(Supplier<? extends L> left) {
    return flatMap(() -> left(left.get()));
  }

  public <L> Either<L, R> flatMap(
      Supplier<? extends Either<? extends L, ? extends R>> choice) {
    return right.<Either<L, R>>map(Either::right)
        .orElseGet(() -> narrow(choice.get()));
  }
}
