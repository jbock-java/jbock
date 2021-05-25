package net.jbock.either;

import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.either.Either.narrow;

public final class MaybeLeft<L> {

  private final Optional<? extends L> left;

  MaybeLeft(Optional<? extends L> left) {
    this.left = left;
  }

  public <R> Either<L, R> orRight(Supplier<? extends R> right) {
    return left.<Either<L, R>>map(Left::create)
        .orElseGet(() -> Right.create(right.get()));
  }

  public <R> Either<L, R> flatMap(
      Supplier<? extends Either<? extends L, ? extends R>> choice) {
    return left.<Either<L, R>>map(Left::create)
        .orElseGet(() -> narrow(choice.get()));
  }
}
