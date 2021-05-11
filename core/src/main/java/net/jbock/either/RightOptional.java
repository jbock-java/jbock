package net.jbock.either;

import java.util.Optional;

public final class RightOptional<R> {

  private final Optional<? extends R> right;

  RightOptional(Optional<? extends R> right) {
    this.right = right;
  }

  public <L> Either<L, R> orLeft(L left) {
    return right.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(left));
  }
}
