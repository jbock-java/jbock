package net.jbock.coerce.either;

import java.util.function.Function;

public abstract class Either<A, B> {

  public static <A, B> Either<A, B> left(A value) {
    return new Left<>(value);
  }

  public static <A, B> Either<A, B> right(B value) {
    return new Right<>(value);
  }

  public <C, D> Either<C, D> map(Function<A, C> f1, Function<B, D> f2) {
    if (this instanceof Left) {
      return Either.left(f1.apply(((Left<A, B>) this).value()));
    }
    return Either.right(f2.apply(((Right<A, B>) this).value()));
  }

  public <C, D> Either<C, D> flatLeftMap(Function<A, Either<C, D>> f1, Function<B, D> f2) {
    if (this instanceof Left) {
      return f1.apply(((Left<A, B>) this).value());
    }
    return Either.right(f2.apply(((Right<A, B>) this).value()));
  }

  public A orElseThrow(Function<B, ? extends RuntimeException> f) {
    if (this instanceof Left) {
      return ((Left<A, B>) this).value();
    }
    throw f.apply(((Right<A, B>) this).value());
  }
}
