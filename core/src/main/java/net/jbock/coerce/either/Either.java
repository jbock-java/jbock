package net.jbock.coerce.either;

import java.util.function.Function;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  public static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  public <L2, R2> Either<L2, R2> map(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
    if (this instanceof Left) {
      return left(leftFunction.apply(((Left<L, R>) this).value()));
    }
    return right(rightFunction.apply(((Right<L, R>) this).value()));
  }

  public <R2> Either<L, R2> map(Function<R, Either<L, R2>> rightFunction) {
    if (this instanceof Left) {
      return left(((Left<L, R>) this).value());
    }
    return rightFunction.apply(((Right<L, R>) this).value());
  }

  public R orElseThrow(Function<L, ? extends RuntimeException> f) {
    if (this instanceof Right) {
      return ((Right<L, R>) this).value();
    }
    throw f.apply(((Left<L, R>) this).value());
  }
}
