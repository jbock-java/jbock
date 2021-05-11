package net.jbock.either;

import java.util.Optional;

public final class LeftOptional<L> {

  private final Optional<? extends L> left;

  LeftOptional(Optional<? extends L> left) {
    this.left = left;
  }

  public <R> Either<L, R> orRight(R right) {
    return left.<Either<L, R>>map(Left::create)
        .orElseGet(() -> Right.create(right));
  }
}
