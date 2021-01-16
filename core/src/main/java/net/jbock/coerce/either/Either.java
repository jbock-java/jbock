package net.jbock.coerce.either;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  public static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  public static <L> Either<L, Void> right() {
    return Right.empty();
  }

  public abstract <R2> Either<L, R2> map(Function<R, R2> rightMapper);

  public abstract <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightMapper);

  public abstract <R2> Either<L, R2> flatMap(Supplier<Either<L, R2>> rightMapper);

  public abstract R orElseThrow(Function<L, ? extends RuntimeException> leftMapper);
}
