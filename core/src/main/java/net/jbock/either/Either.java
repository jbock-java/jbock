package net.jbock.either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return Left.create(value);
  }

  public static <L, R> Either<L, R> left() {
    return Left.nothing();
  }

  public static <L, R> Either<L, R> right(R value) {
    return Right.create(value);
  }

  public static <L> Either<L, Void> right() {
    return Right.containsNull();
  }

  public static <L, R> Either<L, R> fromOptional(L emptyValue, Optional<R> optional) {
    return optional.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(emptyValue));
  }

  public abstract boolean isPresent();

  public abstract <R2> Either<L, R2> map(Function<R, R2> rightMapper);

  public abstract <R2> Either<L, R2> choose(Function<R, Either<L, R2>> rightMapper);

  public abstract <R2> Either<L, R2> choose(Supplier<Either<L, R2>> rightMapper);

  public abstract <L2> Either<L2, R> mapLeft(Function<L, L2> leftMapper);

  public abstract <L2> Either<L2, R> chooseLeft(Function<L, Either<L2, R>> leftMapper);

  public abstract Either<R, L> swap();

  public abstract Either<L, Void> ifPresent(Consumer<R> rightConsumer);

  public abstract R orElseThrow(Function<L, ? extends RuntimeException> leftMapper);
}
