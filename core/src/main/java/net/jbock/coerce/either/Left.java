package net.jbock.coerce.either;

import java.util.Optional;

public class Left<A, B> extends Either<A, B> {

  private final A value;

  Left(A a) {
    value = a;
  }

  public A value() {
    return value;
  }

  @Override
  public boolean isLeft() {
    return true;
  }

  @Override
  public Optional<A> getLeft() {
    return Optional.of(value);
  }

  @Override
  public Optional<B> getRight() {
    return Optional.empty();
  }
}
