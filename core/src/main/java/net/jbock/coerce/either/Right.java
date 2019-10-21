package net.jbock.coerce.either;

public class Right<A, B> extends Either<A, B> {

  private final B value;

  Right(B b) {
    value = b;
  }

  public B value() {
    return value;
  }
}
