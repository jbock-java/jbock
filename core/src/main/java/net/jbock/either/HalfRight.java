package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.either.Either.left;

public final class HalfRight<R> {

  private final Optional<? extends R> right;

  HalfRight(Optional<? extends R> right) {
    this.right = right;
  }

  public <L> Either<L, R> orElseLeft(Supplier<? extends L> left) {
    return right.<Either<L, R>>map(Right::create)
        .orElseGet(() -> left(left.get()));
  }
}
