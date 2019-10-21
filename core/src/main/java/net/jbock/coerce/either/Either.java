package net.jbock.coerce.either;

import java.util.function.Function;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  public static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  public <A, B> Either<A, B> map(Function<L, A> leftFunction, Function<R, B> rightFunction) {
    if (this instanceof Left) {
      return Either.left(leftFunction.apply(((Left<L, R>) this).value()));
    }
    return Either.right(rightFunction.apply(((Right<L, R>) this).value()));
  }

  public <A, B> Either<A, B> flatRightMap(Function<L, A> f1, Function<R, Either<A, B>> f2) {
    if (this instanceof Right) {
      return f2.apply(((Right<L, R>) this).value());
    }
    return Either.left(f1.apply(((Left<L, R>) this).value()));
  }

  public R orElseThrow(Function<L, ? extends RuntimeException> f) {
    if (this instanceof Right) {
      return ((Right<L, R>) this).value();
    }
    throw f.apply(((Left<L, R>) this).value());
  }
}
