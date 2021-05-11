package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

public final class LeftOptional<R> {

  private final Optional<? extends R> right;

  LeftOptional(Optional<? extends R> right) {
    this.right = right;
  }

  public <L> Either<L, R> orElse(L left) {
    return orElseGet(() -> left);
  }

  public <L> Either<L, R> orElseGet(Supplier<? extends L> left) {
    return right.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(left.get()));
  }
}
