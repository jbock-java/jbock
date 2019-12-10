package net.jbock.coerce.either;

import java.util.function.Function;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  public static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  public abstract <L2, R2> Either<L2, R2> map(Function<L, L2> leftMapper, Function<R, R2> rightMapper);

  public abstract <L2, R2> Either<L2, R2> flatMap(Function<L, L2> leftMapper, Function<R, Either<L2, R2>> rightMapper);

  public abstract <U> U fold(Function<L, U> leftMapper, Function<R, U> rightMapper);

  public abstract R orElseThrow(Function<L, ? extends Throwable> f);
}
