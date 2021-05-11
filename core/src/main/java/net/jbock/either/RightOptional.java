package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

public final class RightOptional<L> {

  private final Optional<? extends L> left;

  RightOptional(Optional<? extends L> left) {
    this.left = left;
  }

  public <R> Either<L, R> orElse(R right) {
    return orElseGet(() -> right);
  }

  public <R> Either<L, R> orElseGet(Supplier<? extends R> right) {
    return left.<Either<L, R>>map(Left::create)
        .orElseGet(() -> Right.create(right.get()));
  }
}
