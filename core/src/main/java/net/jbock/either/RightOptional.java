package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

public final class RightOptional<R> {

  private final Optional<? extends R> right;

  RightOptional(Optional<? extends R> right) {
    this.right = right;
  }

  public <L> Either<L, R> orLeft(Supplier<? extends L> left) {
    return right.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(left.get()));
  }
}
