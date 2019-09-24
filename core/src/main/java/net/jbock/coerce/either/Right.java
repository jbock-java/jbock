package net.jbock.coerce.either;

import java.util.Optional;

public class Right<A, B> extends Either<A, B> {

  private final B value;

  Right(B b) {
    value = b;
  }

  public B value() {
    return value;
  }

  @Override
  public boolean isLeft() {
    return false;
  }

  @Override
  public Optional<A> getLeft() {
    return Optional.empty();
  }

  @Override
  public Optional<B> getRight() {
    return Optional.of(value);
  }
}
