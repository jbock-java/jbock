package net.jbock.coerce.either;

import java.util.function.Function;

public class Right<L, R> extends Either<L, R> {

  private final R value;

  Right(R b) {
    value = b;
  }

  public R value() {
    return value;
  }

  @Override
  public <L2, R2> Either<L2, R2> map(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
    return right(rightFunction.apply(value));
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, Either<L, R2>> rightFunction) {
    return rightFunction.apply(value);
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> f) {
    return value;
  }
}
