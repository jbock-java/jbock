package net.jbock.coerce.either;

import java.util.function.Function;

public class Left<L, R> extends Either<L, R> {

  private final L value;

  Left(L a) {
    value = a;
  }

  public L value() {
    return value;
  }

  @Override
  public <L2, R2> Either<L2, R2> map(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
    return left(leftFunction.apply(value));
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, Either<L, R2>> rightFunction) {
    return left(value);
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> f) {
    throw f.apply(value);
  }
}
