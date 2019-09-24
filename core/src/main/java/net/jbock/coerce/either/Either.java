package net.jbock.coerce.either;

import java.util.Optional;

public abstract class Either<A, B> {

  public static <A, B> Either<A, B> left(A value) {
    return new Left<>(value);
  }

  public static <A, B> Either<A, B> right(B value) {
    return new Right<>(value);
  }

  public abstract boolean isLeft();

  public abstract Optional<A> getLeft();

  public abstract Optional<B> getRight();

}
