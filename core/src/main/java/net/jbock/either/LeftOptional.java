package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

public final class LeftOptional<L> {

  private final Optional<? extends L> left;

  LeftOptional(Optional<? extends L> left) {
    this.left = left;
  }

  public <R> Either<L, R> orRight(Supplier<? extends R> right) {
    return left.<Either<L, R>>map(Left::create)
        .orElseGet(() -> Right.create(right.get()));
  }

  public <R2> Either<L, R2> flatMap(
      Supplier<? extends Either<? extends L, ? extends R2>> choice) {
    return orRight(() -> null).flatMap(nothing -> choice.get());
  }
}
